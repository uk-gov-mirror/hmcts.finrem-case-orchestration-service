package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentgenerator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentConversionException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentConversionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class DocumentConversionServiceTest {

    public static final String PDF_SERVICE_URI = "http://localhost:4001/rs/convert";
    public static final byte[] CONVERTED_BINARY = "converted".getBytes();
    public static final String AUTH = "auth";

    @Autowired
    private DocumentConversionService documentConversionService;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private EvidenceManagementDownloadService evidenceManagementService;

    private MockRestServiceServer mockServer;

    private Document documentToConvert;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        documentToConvert = new Document();
        documentToConvert.setFileName("file.docx");
        documentToConvert.setUrl("docUrl.com");
        documentToConvert.setBinaryUrl("binaryUrl.com");
    }

    @Test
    public void fixPdfXCompliance() throws IOException {

        File pdfFile = new File("src/test/resources/fixtures/non-compliant.pdf");

        System.out.println("---- PDF Validation before fix ----");
        runPreFlightCheck(pdfFile);

//        correctPdfErrors(pdfFile);
//
//        System.out.println("---- PDF Validation after fix ----");
//        runPreFlightCheck(new File("fixed-pdf.pdf"));

    }

    private void runPreFlightCheck(File file){

        String outputFile = "pdf_validation_errors.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            // Parse the PDF file to get the validation result
            ValidationResult result = PreflightParser.validate(file);
            writer.write("Validating " + file.getName());
            if (result.isValid()) {
                writer.write("PDF is valid and meets PDF/X standards.");
                writer.newLine();
            } else {
                writer.write("PDF is not valid and does not meet PDF/X standards.");
                writer.newLine();

                // Get the list of errors
                var errors = result.getErrorsList();
                writer.write("Number of errors: " + errors.size());
                writer.newLine();
                writer.newLine();

                // Write each error to the file
                for (ValidationResult.ValidationError error : errors) {
                    writer.write("Error: " + error.getDetails());
                    writer.newLine();
                }
            }

            System.out.println("Validation result written to file: " + outputFile);
        } catch (IOException e) {
            System.err.println("Error while validating PDF or writing to file: " + e.getMessage());
        }
    }

    private void correctPdfErrors(File file) throws IOException {
        PDDocument document = Loader.loadPDF(file);

        for (PDPage page : document.getPages()) {
            // Get resources and fonts used in the page
            PDResources resources = page.getResources();
            resources.getFontNames().forEach(fontName -> {
                try {
                    // Get the font object for the specific font name
                    PDFont font = resources.getFont(fontName);

                    // If the font is Helvetica and is not embedded, replace it with the embedded one
                    if (font.getName().equals("Helvetica") && !font.isEmbedded()) {
                        // Load the built-in Helvetica font
                        InputStream helveticaStream = PDType0Font.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/Helvetica.ttf");
                        PDFont embeddedFont = PDType0Font.load(document, helveticaStream);

                        // Replace all occurrences of the non-embedded font with the embedded version
                        resources.put(fontName, embeddedFont);
                    }

                    // If the font is ZapfDingbats and is not embedded, replace it with an embedded font (e.g., Symbol)
                    if (font.getName().equals("ZapfDingbats") && !font.isEmbedded()) {
                        // Load the built-in Symbol font
                        InputStream symbolFontStream = PDType0Font.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/Arial.ttf"); // Or another embedded font
                        PDFont embeddedFont = PDType0Font.load(document, symbolFontStream);

                        // Replace all occurrences of the non-embedded ZapfDingbats font with the embedded version
                        resources.put(fontName, embeddedFont);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        // Save the fixed PDF
        document.save("fixed-pdf.pdf");
        System.out.println("Fixed PDF saved as 'fixed-pdf.pdf'.");
    }

    @Test
    public void testFlattenPdfDocument() throws IOException {

        byte[] editedPdfBytes = loadResource("/fixtures/D11Edited.pdf");

        // Ensure the original PDF has an AcroForm and at least one form field
        try (PDDocument originalDoc = Loader.loadPDF(editedPdfBytes)) {
            PDAcroForm originalAcroForm = originalDoc.getDocumentCatalog().getAcroForm();
            assertNotNull("Document should have an AcroForm", originalAcroForm);
            assertFalse("Document should have form fields", originalAcroForm.getFields().isEmpty());
        }

        // Flatten the PDF using the method under test
        byte[] flattenedPdfBytes = documentConversionService.flattenPdfDocument(editedPdfBytes);

        // Load the flattened PDF and check that form fields have been removed/flattened
        try (PDDocument flattenedDoc = Loader.loadPDF(flattenedPdfBytes)) {
            PDAcroForm flattenedAcroForm = flattenedDoc.getDocumentCatalog().getAcroForm();
            assertTrue("AcroForm should be flattened", ObjectUtils.isEmpty(flattenedAcroForm.getFields()));
        }
    }

    @Test
    public void doNotFlattenPdfDocumentWithNoFromLayer() throws IOException {

        String editedPdfFixture = "/fixtures/D81_consent_order.pdf";
        byte[] pdfBytes = loadResource(editedPdfFixture);
        byte[] result = documentConversionService.flattenPdfDocument(pdfBytes);

        assertThat(pdfBytes, is(result));
    }

    @Test
    public void flattenNonPdfDocumentHandleException() throws IOException {

        String toBeFlattenedFile = "/fixtures/MockD11Word.docx";
        byte[] toBeFlattenedbytes = loadResource(toBeFlattenedFile);

        byte[] result = documentConversionService.flattenPdfDocument(toBeFlattenedbytes);

        assertThat(toBeFlattenedbytes, is(result));
    }

    @Test
    public void convertWordToPdf() {
        mockServer.expect(requestTo(PDF_SERVICE_URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(CONVERTED_BINARY, MediaType.APPLICATION_OCTET_STREAM));

        when(
            evidenceManagementService.download(documentToConvert.getBinaryUrl(), AUTH))
            .thenReturn("bytes".getBytes());

        byte[] result = documentConversionService.convertDocumentToPdf(documentToConvert, AUTH);
        assertThat(result, is(notNullValue()));
        assertThat(result, is(CONVERTED_BINARY));
    }

    @Test(expected = DocumentConversionException.class)
    public void convertWordToPdfFailsWhenAlreadyPdf() throws Exception {
        mockServer.expect(requestTo(PDF_SERVICE_URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(CONVERTED_BINARY, MediaType.APPLICATION_OCTET_STREAM));

        when(
            evidenceManagementService.download(documentToConvert.getBinaryUrl(), AUTH))
            .thenReturn("bytes".getBytes());

        documentToConvert.setFileName("file.pdf");
        byte[] result = documentConversionService.convertDocumentToPdf(documentToConvert, AUTH);
    }

    @Test
    public void getConvertedFilename() {
        assertThat(documentConversionService.getConvertedFilename("nodot"), is("nodot.pdf"));
        assertThat(documentConversionService.getConvertedFilename("word.docx"), is("word.pdf"));
    }

    private byte[] loadResource(String testPdf) throws IOException {

        try (InputStream resourceAsStream = getClass().getResourceAsStream(testPdf)) {
            assert resourceAsStream != null;
            return resourceAsStream.readAllBytes();
        }
    }
}

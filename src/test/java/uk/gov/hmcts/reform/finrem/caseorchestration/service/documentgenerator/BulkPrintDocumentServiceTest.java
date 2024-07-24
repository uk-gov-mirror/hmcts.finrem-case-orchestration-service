package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentgenerator;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentConversionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkPrintDocumentServiceTest {

    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private static final String FILE_BINARY_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ/binary";
    private static final String FILE_NAME = "abc.pdf";
    private static final String DOC_FILE_NAME = "abc.docx";
    public static final String AUTH = "auth";
    private final byte[] someBytes = "ainhsdcnoih".getBytes();
    @InjectMocks
    private BulkPrintDocumentService service;

    @Mock
    private EvidenceManagementDownloadService evidenceManagementService;
    @Mock
    private DocumentConversionService documentConversionService;


    @Test
    void downloadDocuments() {
        when(evidenceManagementService.download(FILE_URL, AUTH)).thenReturn(someBytes);

        BulkPrintRequest bulkPrintRequest = BulkPrintRequest.builder()
            .bulkPrintDocuments(singletonList(BulkPrintDocument.builder()
                .binaryFileUrl(FILE_URL)
                .fileName(FILE_NAME)
                .build()))
            .build();

        List<byte[]> result = service.downloadDocuments(bulkPrintRequest, AUTH);
        assertThat(result.get(0), is(equalTo(someBytes)));
    }

    @Test
    void validateWordDocumentOnUploadedDocument() {
        Document document = Document.builder().url(FILE_URL)
            .binaryUrl(FILE_BINARY_URL)
            .fileName(DOC_FILE_NAME)
            .build();
        when(documentConversionService.convertDocumentToPdf(document, AUTH)).thenReturn(someBytes);
        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, DOC_FILE_NAME, FILE_BINARY_URL);

        List<String> errors = new ArrayList<>();
        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertTrue(errors.get(0).contains("Failed to parse the documents for abc.docx"));
    }

    @Test
    void validateEncryptionOnUploadedDocumentAddErrorOnMessage() throws IOException {
        String fixture = "/fixtures/encryptedDocument.pdf";
        byte[] bytes = loadResource(fixture);
        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        when(evidenceManagementService.download(FILE_BINARY_URL, AUTH)).thenReturn(bytes);
        List<String> errors = new ArrayList<>();

        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertEquals("Uploaded document 'abc.pdf' contains some kind of encryption. "
            + "Please remove encryption before uploading or upload another document.", errors.get(0));
    }

    @Test
    void validatePasswordProtectedDocumentUploadedThenThrowPasswordProtectedMessage() throws IOException {
        String fixture = "/fixtures/go1protected.pdf";
        byte[] bytes = loadResource(fixture);
        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        when(evidenceManagementService.download(FILE_BINARY_URL, AUTH)).thenReturn(bytes);
        List<String> errors = new ArrayList<>();

        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertEquals("Uploaded document 'abc.pdf' is password protected. "
            + "Please remove password and try uploading again.", errors.get(0));
    }

    @Test
    void validateEmptyUploadedFileThenDisplayMessage() throws IOException {
        byte[] bytes = null;
        CaseDocument caseDocument = TestSetUpUtils.caseDocument(FILE_URL, FILE_NAME, FILE_BINARY_URL);

        when(evidenceManagementService.download(FILE_BINARY_URL, AUTH)).thenReturn(bytes);
        List<String> errors = new ArrayList<>();

        service.validateEncryptionOnUploadedDocument(caseDocument, "1234", errors, AUTH);
        assertEquals("Uploaded document abc.pdf is empty.", errors.get(0));
    }

    private byte[] loadResource(String testPdf) throws IOException {

        try (InputStream resourceAsStream = getClass().getResourceAsStream(testPdf)) {
            assert resourceAsStream != null;
            return resourceAsStream.readAllBytes();
        }
    }

    @Test
    public void testPdfFonts() throws IOException {
        String editedPdfFixture = "/fixtures/encryptedDocument.pdf";
        byte[] editedPdfBytes = loadResource(editedPdfFixture);

        try (PDDocument document = PDDocument.load(editedPdfBytes)) {
            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                for (COSName fontName : resources.getFontNames()) {

                    PDFont font = resources.getFont(fontName);

                    // Get the font descriptor
                    PDFontDescriptor fontDescriptor = font.getFontDescriptor();
                    if (fontDescriptor.getFontFile() != null) {
                        System.out.println("Font File Type 1 is present.");
                    }
                    if (fontDescriptor.getFontFile2() != null) {
                        System.out.println("TrueType Font File is present.");
                    }
                    if (fontDescriptor.getFontFile3() != null) {
                        System.out.println("Other Font File (not Type 1 or TrueType) is present.");
                    }
                    System.out.println(font.getName());
                    System.out.println("Damaged: " + font.isDamaged());
                    if (font.isEmbedded()) {
                        System.out.println("Embedded font: " + font.getName());
                    } else {
                        System.out.println("Non-embedded font: " + font.getName());
                    }
                    System.out.println("\n");

                }
            }
        }
    }

    @Test
    public void testPdfValid() throws IOException {

        ValidationResult result;
        String editedPdfFixture =
                "/explicit/path/to/a/file].pdf";
        File thePdfFile = new File(editedPdfFixture);
        PreflightParser parser = new PreflightParser(thePdfFile);
        try {

            /* Parse the PDF file with PreflightParser that inherits from the NonSequentialParser.
             * Some additional controls are present to check a set of PDF/A requirements.
             * (Stream length consistency, EOL after some Keyword...)
             */
            parser.parse();

            /* Once the syntax validation is done,
             * the parser can provide a PreflightDocument
             * (that inherits from PDDocument)
             * This document process the end of PDF/A validation.
             */
            PreflightDocument document = parser.getPreflightDocument();
            document.validate();

            // Get validation result
            result = document.getResult();
            document.close();

        } catch (SyntaxValidationException e) {
            /* the parse method can throw a SyntaxValidationException
             * if the PDF file can't be parsed.
             * In this case, the exception contains an instance of ValidationResult
             */
            result = e.getResult();
        }

        // display validation result
        if (result.isValid()) {
            System.out.println("The file " + thePdfFile.getName() + " is a valid PDF/A-1b file");
        } else {
            System.out.println("The file " + thePdfFile.getName() + " is not valid, error(s) :");
            for (ValidationResult.ValidationError error : result.getErrorsList()) {
                System.out.println(error.getErrorCode() + " : " + error.getDetails());
            }
        }

    }
}

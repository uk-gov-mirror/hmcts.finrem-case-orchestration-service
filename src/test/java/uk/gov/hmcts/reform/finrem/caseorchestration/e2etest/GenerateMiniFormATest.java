package uk.gov.hmcts.reform.finrem.caseorchestration.e2etest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentRequest;

import java.util.ArrayList;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GenerateMiniFormATest extends BaseE2ETest {

    private static final String AUTH_TOKEN = "LIUGBYUFUYVbnvhchvUFJHVJBlBJHBUYCUV";
    private static final String API_URL = "/case-orchestration/generate-mini-form-a";
    private static final String GENERATE_DOCUMENT_CONTEXT_PATH = "/version/1/generatePDF";
    private static final String URL = "http://dm-store/lhjbyuivu87y989hijbb";
    private static final String BINARY_URL = "http://dm-store/lhjbyuivu87y989hijbb/binary";
    private static final String FILE_NAME = "app_docs.pdf";

    @Value("${document.miniFormA.template}")
    private String miniFormATemplate;

    @ClassRule
    public static WireMockClassRule documentGeneratorService = new WireMockClassRule(4009);

    @Test
    public void generateMiniFormA() throws Exception {
        generateDocumentServiceSuccessStub();

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedCaseData()));
    }

    @Test
    public void documentGeneratorServiceError() throws Exception {
        generateDocumentServiceErrorStub();

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(objectMapper.writeValueAsString(request))
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    private String expectedCaseData() throws JsonProcessingException {
        CaseDetails caseDetails = request.getCaseDetails();
        caseDetails.getCaseData().setMiniFormA(caseDocument());

        return objectMapper.writeValueAsString(new CCDCallbackResponse(caseDetails.getCaseData(),
                new ArrayList<>(), new ArrayList<>()));
    }

    private CaseDocument caseDocument() {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentBinaryUrl(BINARY_URL);
        caseDocument.setDocumentFilename(FILE_NAME);
        caseDocument.setDocumentUrl(URL);
        return caseDocument;
    }

    private DocumentRequest documentRequest() {
        return DocumentRequest.builder()
                .template(miniFormATemplate)
                .values(Collections.singletonMap("caseDetails", request.getCaseDetails()))
                .build();
    }

    private Document document() {
        Document document = new Document();
        document.setBinaryUrl(BINARY_URL);
        document.setCreatedOn("22 Oct 2018");
        document.setFileName(FILE_NAME);
        document.setMimeType("application/pdf");
        document.setUrl(URL);

        return document;
    }

    private void generateDocumentServiceSuccessStub() throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(documentRequest()), true, true))
                .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
                .withHeader(CONTENT_TYPE, equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(objectMapper.writeValueAsString(document()))));
    }

    private void generateDocumentServiceErrorStub() throws JsonProcessingException {
        documentGeneratorService.stubFor(post(urlPathEqualTo(GENERATE_DOCUMENT_CONTEXT_PATH))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(documentRequest()), true, true))
                .withHeader(AUTHORIZATION, equalTo(AUTH_TOKEN))
                .withHeader(CONTENT_TYPE, equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)));
    }
}

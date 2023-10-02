package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CreateGeneralLetterAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class CreateGeneralLetterAboutToStartHandlerIntegrationTest extends BaseTest {

    @Autowired
    private IdamService idamService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CreateGeneralLetterAboutToStartHandler createGeneralLetterAboutToStartHandler;

    @Test
    public void shouldSetDefaultValues() throws Exception {

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = createGeneralLetterAboutToStartHandler.handle(FinremCallbackRequest.builder()
                .eventType(EventType.CREATE_GENERAL_LETTER_JUDGE)
            .caseDetails(FinremCaseDetails.builder()
                .id(123L).data(FinremCaseData.builder()
                    .generalLetterWrapper(GeneralLetterWrapper.builder()
                        .generalLetterAddressTo(GeneralLetterAddressToType.APPLICANT_SOLICITOR)
                        .generalLetterRecipient("Test")
                        .generalLetterRecipientAddress(Address.builder()
                            .addressLine1("line1")
                            .addressLine2("line2")
                            .country("country")
                            .postCode("AB1 1BC").build())
                        .generalLetterCreatedBy("Test")
                        .generalLetterBody("body")
                        .generalLetterPreview(CaseDocument.builder().build())
                        .build())
                    .build())
                .build())
            .build(), "");
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterAddressTo());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterRecipient());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterRecipientAddress());
        assertEquals("UserName", response.getData().getGeneralLetterWrapper().getGeneralLetterCreatedBy());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterBody());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterPreview());
        assertNull(response.getData().getGeneralLetterWrapper().getGeneralLetterUploadedDocument());
        assertEquals("Username", idamService.getIdamFullName(anyString()));
    }
}

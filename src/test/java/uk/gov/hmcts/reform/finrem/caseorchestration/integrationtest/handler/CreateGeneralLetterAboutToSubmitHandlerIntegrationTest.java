package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CreateGeneralLetterAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralLetterAddressToType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralLetterWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralLetterService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class CreateGeneralLetterAboutToSubmitHandlerIntegrationTest extends BaseTest {

    @Autowired
    private GeneralLetterService generalLetterService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Autowired
    private CreateGeneralLetterAboutToSubmitHandler createGeneralLetterAboutToSubmitHandler;


    @Test
    public void shouldSetDefaultValues() throws Exception {

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = createGeneralLetterAboutToSubmitHandler.handle(FinremCallbackRequest.builder()
            .eventType(EventType.CREATE_GENERAL_LETTER)
            .caseDetails(FinremCaseDetails.builder()
                .id(123L).caseType(CaseType.CONSENTED)
                .data(FinremCaseData.builder()
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
        verify(generalLetterService).getCaseDataErrorsForCreatingPreviewOrFinalLetter(any(FinremCaseDetails.class));
    }
}

package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.IssueApplicationContestedAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.TypeOfApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnlineFormDocumentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class IssueApplicationContestedAboutToSubmitHandlerIntegrationTest extends BaseTest {

    @Autowired
    private OnlineFormDocumentService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IssueApplicationContestedAboutToSubmitHandler issueApplicationContestedAboutToSubmitHandler;

    @Test
    public void generateAppropriateCaseDocumentAndSetDefaultIfValueIsMissing() throws Exception{

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = issueApplicationContestedAboutToSubmitHandler.handle(FinremCallbackRequest.builder()
            .eventType(EventType.ISSUE_APPLICATION)
            .caseDetails(FinremCaseDetails.builder()
                .id(123L).data(FinremCaseData.builder()
                    .scheduleOneWrapper(ScheduleOneWrapper.builder()
                        .build())
                    .build())
                .build())
            .build(),"");

        assertEquals("123", response.getData().getDivorceCaseNumber());
        assertEquals(TypeOfApplication.MATRIMONIAL_CIVILPARTNERSHIP.getTypeOfApplication(),
            response.getData().getScheduleOneWrapper().getTypeOfApplication().getValue());
        assertEquals(caseDocument(), response.getData().getMiniFormA());

    }

    @Test
    public void generateAppropriateCaseDocumentDoNotSetIfAlreadySetValue() throws Exception{

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = issueApplicationContestedAboutToSubmitHandler.handle(FinremCallbackRequest.builder()
            .eventType(EventType.ISSUE_APPLICATION)
            .caseDetails(FinremCaseDetails.builder()
                .id(123L).data(FinremCaseData.builder().divorceCaseNumber("897901").scheduleOneWrapper(ScheduleOneWrapper.builder()
                    .typeOfApplication(Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989)
                    .build()).build()).build())
            .build(),"");

        assertEquals("897901", response.getData().getDivorceCaseNumber());
        assertEquals(TypeOfApplication.SCHEDULE_ONE.getTypeOfApplication(),
            response.getData().getScheduleOneWrapper().getTypeOfApplication().getValue());
        assertEquals(caseDocument(), response.getData().getMiniFormA());

    }
}

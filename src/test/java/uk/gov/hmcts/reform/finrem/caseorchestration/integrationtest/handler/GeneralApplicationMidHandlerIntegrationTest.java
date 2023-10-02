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
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.GeneralApplicationMidHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.GeneralApplicationItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class GeneralApplicationMidHandlerIntegrationTest extends BaseTest {

    @Autowired
    private GenericDocumentService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GeneralApplicationMidHandler generalApplicationMidHandler;

    @Test
    public void generalApplicationEventStartButNotAddedDetails_thenThrowErrorMessage() throws Exception{

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = generalApplicationMidHandler.handle(FinremCallbackRequest.builder()
            .eventType(EventType.GENERAL_APPLICATION)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build(),"" );
        assertTrue(response.getErrors().get(0)
            .contains("Please complete the General Application. No information has been entered for this application."));
    }

    @Test
    public void generalApplicationEventStartAndThereIsExistingApplicationButNotAddedNewApplication_thenThrowErrorMessage() throws Exception{

        GeneralApplicationsCollection record1 = GeneralApplicationsCollection.builder().id(UUID.randomUUID())
            .value(GeneralApplicationItems.builder().generalApplicationCreatedBy("Test1").build()).build();
        GeneralApplicationsCollection record2 = GeneralApplicationsCollection.builder().id(UUID.randomUUID())
            .value(GeneralApplicationItems.builder().generalApplicationCreatedBy("Test2").build()).build();

        GeneralApplicationWrapper wrapper1 = GeneralApplicationWrapper.builder().generalApplications(List.of(record1, record2)).build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = generalApplicationMidHandler.handle(FinremCallbackRequest.builder()
            .eventType(EventType.GENERAL_APPLICATION)
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(FinremCaseData.builder().generalApplicationWrapper(wrapper1).build()).build())
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(FinremCaseData.builder().generalApplicationWrapper(wrapper1).build()).build())
            .build(),"" );
    }
}

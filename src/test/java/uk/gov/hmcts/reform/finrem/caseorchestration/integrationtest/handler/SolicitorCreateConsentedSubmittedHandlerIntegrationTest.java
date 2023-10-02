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
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.SolicitorCreateConsentedSubmittedHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CreateCaseService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class SolicitorCreateConsentedSubmittedHandlerIntegrationTest extends BaseTest {

    @Autowired
    private CreateCaseService createCaseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SolicitorCreateConsentedSubmittedHandler solicitorCreateConsentedSubmittedHandler;

    @Test
    public void shouldSetDefaultValues() throws Exception{

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response
            = solicitorCreateConsentedSubmittedHandler.handle(FinremCallbackRequest.builder()
            .eventType(EventType.SOLICITOR_CREATE)
            .caseDetails(FinremCaseDetails.builder()
                .id(123L)
                .data(FinremCaseData.builder()
                    .build())
                .build())
            .build(),"" );
        Assert.assertEquals(123L, response.getData());

    }
}

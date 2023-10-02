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
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.SolicitorCreateConsentedAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_CONSENT_ORDER;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class SolicitorCreateConsentedAboutToSubmitHandlerIntegrationTest extends BaseTest {

    @Autowired
    private ConsentOrderService consentOrderService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private CaseFlagsService caseFlagsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SolicitorCreateConsentedAboutToSubmitHandler solicitorCreateConsentedAboutToSubmitHandler;

    @Test
    public void shouldSetToDefault() throws Exception{

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response
            = solicitorCreateConsentedAboutToSubmitHandler.handle(CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(new HashMap<>())
                .build())
            .build(),"");
        Assert.assertEquals(YES_VALUE, response.getData().get(APPLICANT_REPRESENTED));
    }

}

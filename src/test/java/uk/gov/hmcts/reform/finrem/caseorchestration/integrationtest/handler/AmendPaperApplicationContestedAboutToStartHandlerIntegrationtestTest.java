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
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.AmendPaperApplicationContestedAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION_DEFAULT_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.URGENT_CASE_QUESTION;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class AmendPaperApplicationContestedAboutToStartHandlerIntegrationtestTest extends BaseTest {

    @Autowired
    private OnStartDefaultValueService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AmendPaperApplicationContestedAboutToStartHandler amendPaperApplicationContestedAboutToStartHandler;

    @Test
    public void shouldSetToDefault() throws Exception{

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response
            = amendPaperApplicationContestedAboutToStartHandler.handle(CallbackRequest.builder()
            .eventId(EventType.AMEND_CONTESTED_PAPER_APP_DETAILS.getCcdType())
            .caseDetails(CaseDetails.builder()
                .id(123L)
                .data(new HashMap<>())
                .build())
            .build(),"");
        Assert.assertEquals(NO_VALUE, response.getData().get(CIVIL_PARTNERSHIP));
        Assert.assertEquals(NO_VALUE, response.getData().get(URGENT_CASE_QUESTION));
        Assert.assertEquals(TYPE_OF_APPLICATION_DEFAULT_TO, response.getData().get(TYPE_OF_APPLICATION));

    }
}

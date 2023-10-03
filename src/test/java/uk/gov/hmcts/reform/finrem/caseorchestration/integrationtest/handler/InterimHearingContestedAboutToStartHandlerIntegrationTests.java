package uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.handler;

import com.fasterxml.jackson.core.type.TypeReference;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.InterimHearingContestedAboutToStartHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.InterimHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.InterimHearingItemMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollectionItemData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_ALL_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DOCUMENT;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Category(IntegrationTest.class)
public class InterimHearingContestedAboutToStartHandlerIntegrationTests extends BaseServiceTest {

    @Autowired
    private InterimHearingHelper interimHearingHelper;

    private static final String TEST_NEW_JSON_WITH_INTERIM_HEARING_DOCS = "/fixtures/contested/interim-hearing-with-interim-hearing-documents.json";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InterimHearingItemMapper interimHearingItemMapper;

    @Autowired
    private PartyService partyService;

    @Autowired
    private InterimHearingContestedAboutToStartHandler interimHearingContestedAboutToStartHandler;

    @Test
    public void shouldSetToDefault() throws Exception {

        CallbackRequest callbackRequest = buildCallbackRequest(TEST_NEW_JSON_WITH_INTERIM_HEARING_DOCS);

        Map<String, Object> data = callbackRequest.getCaseDetails().getData();

        data.put(INTERIM_HEARING_DOCUMENT, CaseDocument.builder()
            .documentBinaryUrl("http://dm-store/lhjbyuivu87y989hijbb/binary")
            .documentFilename("app_docs.pdf")
            .documentUrl("http://dm-store/lhjbyuivu87y989hijbb/binary")
            .build());

        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response
            = interimHearingContestedAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);

        List<InterimHearingCollectionItemData> interimHearingCollectionItemData =
            objectMapper.convertValue(response.getData().get(INTERIM_HEARING_COLLECTION),
                new TypeReference<>() {
                });
        Assert.assertEquals(interimHearingCollectionItemData.size(), 1);


        List<InterimHearingBulkPrintDocumentsData> interimHearingBulkPrintDocumentsDataList =
            objectMapper.convertValue(response.getData().get(INTERIM_HEARING_ALL_DOCUMENT), new TypeReference<>() {
            });
        Assert.assertEquals(interimHearingBulkPrintDocumentsDataList.size(), 2);


    }


}

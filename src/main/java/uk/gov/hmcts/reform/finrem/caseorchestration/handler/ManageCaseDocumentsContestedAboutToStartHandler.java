package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.LegacyConfidentialDocumentsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ManageCaseDocumentsContestedAboutToStartHandler extends FinremCallbackHandler {


    private final LegacyConfidentialDocumentsService legacyConfidentialDocumentsService;

    public ManageCaseDocumentsContestedAboutToStartHandler(FinremCaseDetailsMapper mapper,
                                                           LegacyConfidentialDocumentsService legacyConfidentialDocumentsService) {
        super(mapper);
        this.legacyConfidentialDocumentsService = legacyConfidentialDocumentsService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.MANAGE_CASE_DOCUMENTS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        caseData.setManageCaseDocumentCollection(caseData.getUploadCaseDocumentWrapper().getAllManageableCollections());

        migrateLegacyConfidentialCaseDocumentFormat(caseData);
        populateMissingConfidentialFlag(caseData);

        //TODO: Move to new event and tidy up and refactor the code, check not disrupting event flow after deleting documents
        List<Optional<CaseDocument>> caseDocuments = new ArrayList<>(Arrays.asList(Optional.ofNullable(caseData.getDivorceUploadEvidence1()), Optional.ofNullable(caseData.getDivorceUploadEvidence2()),
                Optional.ofNullable(caseData.getDivorceUploadPetition()), Optional.ofNullable(caseData.getVariationOrderDocument()), Optional.ofNullable(caseData.getAdditionalListOfHearingDocuments()),
                Optional.ofNullable(caseData.getGeneralApplicationWrapper().getGeneralApplicationDirectionsDocument()), Optional.ofNullable(caseData.getGeneralApplicationWrapper().getGeneralApplicationDocument()),
                Optional.ofNullable(caseData.getGeneralApplicationWrapper().getGeneralApplicationDraftOrder()), Optional.ofNullable(caseData.getConsentVariationOrderDocument()),
                Optional.ofNullable(caseData.getAdditionalDocument()), Optional.ofNullable(caseData.getRefusalOrderPreviewDocument()), Optional.ofNullable(caseData.getGeneralOrderWrapper().getGeneralOrderPreviewDocument()),
                Optional.ofNullable(caseData.getConsentOrder()), Optional.ofNullable(caseData.getConsentOrderWrapper().getConsentD81Joint()), Optional.ofNullable(caseData.getConsentOrderWrapper().getConsentD81Applicant()),
                Optional.ofNullable(caseData.getConsentOrderWrapper().getConsentD81Respondent()), Optional.ofNullable(caseData.getOrderRefusalPreviewDocument()),
                Optional.ofNullable(caseData.getInterimWrapper().getInterimUploadAdditionalDocument())));
        List<String> documentTypes = List.of("divorceUploadEvidence1", "divorceUploadEvidence2", "divorceUploadPetition", "variationOrderDocument", "additionalListOfHearingDocuments",
            "generalApplicationDirectionsDocument", "generalApplicationDocument", "generalApplicationDraftOrder", "consentVariationOrderDocument", "additionalDocument", "refusalOrderPreviewDocument",
            "generalOrderPreviewDocument", "consentOrder", "consentD81Joint", "consentD81Applicant", "consentD81Respondent", "orderRefusalPreviewDocument", "interimUploadAdditionalDocument");
        List<CaseDocument> hearingOrderOtherDocuments = new ArrayList<>();
        if (caseData.getHearingOrderOtherDocuments() != null && !caseData.getHearingOrderOtherDocuments().isEmpty()) {
            caseData.getHearingOrderOtherDocuments().forEach(
                caseDocument ->
                    hearingOrderOtherDocuments.add(caseDocument.getValue()));
        }
        List<CaseDocument> additionalCicDocuments = new ArrayList<>();
        if (caseData.getAdditionalCicDocuments() != null && !caseData.getAdditionalCicDocuments().isEmpty()) {
            caseData.getAdditionalCicDocuments().forEach(caseDocument -> additionalCicDocuments.add(caseDocument.getValue()));
        }

        List<DynamicMultiSelectListElement> listElements = new ArrayList<>();
        caseDocuments.forEach(caseDocument -> {
            if (caseDocument.isPresent()) {
                int index = caseDocuments.indexOf(caseDocument);
                DynamicMultiSelectListElement dynamicMultiSelectListElement =
                    DynamicMultiSelectListElement.builder().code(documentTypes.get(index)).label(caseDocument.get().getDocumentFilename()).build();
                listElements.add(dynamicMultiSelectListElement);
            }
        });
        hearingOrderOtherDocuments.forEach(document ->
        {
            DynamicMultiSelectListElement dynamicMultiSelectListElement =
                DynamicMultiSelectListElement.builder().code("hearingOrderOtherDocument").label(document.getDocumentFilename()).build();
            listElements.add(dynamicMultiSelectListElement);
        });
        additionalCicDocuments.forEach(document ->
        {
            DynamicMultiSelectListElement dynamicMultiSelectListElement =
                DynamicMultiSelectListElement.builder().code("additionalCicDocument").label(document.getDocumentFilename()).build();
            listElements.add(dynamicMultiSelectListElement);
        });

        DynamicMultiSelectList dynamicList = DynamicMultiSelectList.builder().listItems(listElements).build();
        caseData.setUploadedDocumentsToDelete(dynamicList);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private void migrateLegacyConfidentialCaseDocumentFormat(FinremCaseData data) {
        if (data.getConfidentialDocumentsUploaded() != null) {
            data.getManageCaseDocumentCollection()
                .addAll(getConfidentialCaseDocumentCollectionFromLegacyConfidentialDocs(data));
            data.getConfidentialDocumentsUploaded().clear();
        }
    }

    private void populateMissingConfidentialFlag(FinremCaseData caseData) {
        caseData.getManageCaseDocumentCollection().stream()
            .filter(this::isConfidentialFlagMissing).forEach(documentCollection ->
                documentCollection.getUploadCaseDocument().setCaseDocumentConfidentiality(YesOrNo.NO));
    }

    private boolean isConfidentialFlagMissing(UploadCaseDocumentCollection documentCollection) {
        return documentCollection.getUploadCaseDocument() != null
            && documentCollection.getUploadCaseDocument().getCaseDocumentConfidentiality() == null;
    }

    private List<UploadCaseDocumentCollection> getConfidentialCaseDocumentCollectionFromLegacyConfidentialDocs(
        FinremCaseData caseData) {
        return legacyConfidentialDocumentsService.mapLegacyConfidentialDocumentToConfidentialDocumentCollection(
            caseData.getConfidentialDocumentsUploaded());
    }
}
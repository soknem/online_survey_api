package com.setec.online_survey.features.share;

import com.setec.online_survey.features.share.dto.QrCodeRequest;
import com.setec.online_survey.features.share.dto.QrCodeResponse;
import com.setec.online_survey.features.share.dto.ShareRequest;
import com.setec.online_survey.features.share.dto.ShareResponse;

public interface ShareService {

    QrCodeResponse generateAndUploadQRCode(QrCodeRequest qrCodeRequest);

    ShareResponse shareSurvey(ShareRequest shareRequest);


}

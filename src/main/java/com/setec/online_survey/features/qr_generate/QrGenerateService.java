package com.setec.online_survey.features.qr_generate;

import com.setec.online_survey.features.qr_generate.dto.QrCodeRequest;
import com.setec.online_survey.features.qr_generate.dto.QrCodeResponse;
import com.setec.online_survey.features.qr_generate.dto.QrGenerateResponse;

public interface QrGenerateService {

    QrCodeResponse generateAndUploadQRCode(QrCodeRequest qrCodeRequest);

}

package com.setec.online_survey.features.share;

import com.setec.online_survey.features.share.dto.QrCodeRequest;
import com.setec.online_survey.features.share.dto.QrCodeResponse;
import com.setec.online_survey.features.share.dto.ShareRequest;
import com.setec.online_survey.features.share.dto.ShareResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping("/qr-code/generate")
    public QrCodeResponse generateQR(@RequestBody @Valid QrCodeRequest qrCodeRequest) {

        return shareService.generateAndUploadQRCode(qrCodeRequest);
    }

    @PostMapping("")
    public ShareResponse share(@RequestBody @Valid ShareRequest shareRequest){

        return shareService.shareSurvey(shareRequest);
    }
}
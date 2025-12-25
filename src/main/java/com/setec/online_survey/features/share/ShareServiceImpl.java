package com.setec.online_survey.features.share;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.setec.online_survey.features.file.FileService;
import com.setec.online_survey.features.file.dto.FileResponse;
import com.setec.online_survey.features.share.dto.QrCodeRequest;
import com.setec.online_survey.features.share.dto.QrCodeResponse;
import com.setec.online_survey.features.share.dto.ShareRequest;
import com.setec.online_survey.features.share.dto.ShareResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final FileService fileService;

    @Value("${media.logo}")
    private String logoPath;

    @Value("${media.survey-share-base-uri}")
    private String baseUri;

    //endpoint that handle manage medias
    @Value("${media.survey-share}")
    private String surveyShare;

    @Override
    public QrCodeResponse generateAndUploadQRCode(QrCodeRequest qrCodeRequest) {
        try {

            int size = 400;
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

            // 1. Generate QR
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrCodeRequest.link(), BarcodeFormat.QR_CODE, size, size, hints);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // 2. Overlay Logo (from static path)
            BufferedImage logo = ImageIO.read(new ClassPathResource(logoPath).getInputStream());
            Graphics2D g = qrImage.createGraphics();
            
            int logoSize = size / 5;
            int x = (size - logoSize) / 2;
            int y = (size - logoSize) / 2;
            
            g.drawImage(logo.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH), x, y, null);
            g.dispose();

            // 3. Convert BufferedImage to MultipartFile
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            CustomMultipartFile multipartFile = new CustomMultipartFile(imageBytes, "qrcode.png");

            FileResponse fileResponse = fileService.uploadSingleFile(multipartFile);

            // 4. Pass to your existing FileService
            return new QrCodeResponse(fileResponse.name(),fileResponse.uri());

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR Code", e);
        }
    }

    @Override
    public ShareResponse shareSurvey(ShareRequest shareRequest) {

        String url= baseUri+surveyShare +shareRequest.shareAlias();

        QrCodeResponse qrCodeResponse = generateAndUploadQRCode(new QrCodeRequest(url));

        String fileName = qrCodeResponse.fileName();
        String qrUrl = qrCodeResponse.url();

        return new ShareResponse(url,fileName,qrUrl);
    }
}
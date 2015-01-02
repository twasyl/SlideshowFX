/*
 * Copyright 2014 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.controls;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This panel is used to display the QR code that redirect the "flasher" to the web app of SlideshowFX.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class QRCodePanel extends VBox {

    private static Logger LOGGER = Logger.getLogger(QRCodePanel.class.getName());

    public QRCodePanel() {
        super(20);

        this.getStylesheets().add(ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/css/qrcode-panel.css"));
        this.getStyleClass().add("qrcode-panel");

        this.setPrefWidth(300);
        this.setMinWidth(300);
        this.setMaxWidth(300);

        final Image qrCode = new Image(new ByteArrayInputStream(this.generateQRCode(250)));
        final ImageView qrCodeView = new ImageView(qrCode);

        final Text flashToChat = new Text("Flash the QR code to access the SlideshowFX web application to interact with the presenter");
        flashToChat.setWrappingWidth(250);
        flashToChat.getStyleClass().add("panel-text");

        final Text url = new Text(String.format("Or access it on http://%1$s:%2$s/slideshowfx",
                SlideshowFXServer.getSingleton().getHost(),
                SlideshowFXServer.getSingleton().getPort()));
        url.setWrappingWidth(250);
        url.getStyleClass().add("panel-text");

        this.setAlignment(Pos.CENTER);

        this.getChildren().addAll(flashToChat, qrCodeView, url);
    }

    /**
     * Generates the QR code that will be used to access the chat. Data of the QR code are simply the URL of the SlideshowFX
     * web application.
     *
     * @param size the size in pixel of the QR code to generate.
     * @return The bytes corresponding to the image of the QR code.
     */
    public byte[] generateQRCode(int size) {
        byte[] qrCode = null;

        if(SlideshowFXServer.getSingleton() != null) {

            final String qrCodeData = String.format("http://%1$s:%2$s%3$s",
                    SlideshowFXServer.getSingleton().getHost(),
                    SlideshowFXServer.getSingleton().getPort(),
                    "/slideshowfx");

            try(final ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                final QRCodeWriter qrWriter = new QRCodeWriter();
                final BitMatrix matrix = qrWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, size, size);

                MatrixToImageWriter.writeToStream(matrix, "png", out);

                out.flush();
                qrCode = out.toByteArray();
            } catch (WriterException | IOException e) {
                LOGGER.log(Level.WARNING, "Can not generate QR Code", e);
            }
        }

        return qrCode;
    }
}

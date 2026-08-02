package services;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;


public class QRScannerService {


    public String scan(BufferedImage image){

        try {

            LuminanceSource source =
                    new BufferedImageLuminanceSource(image);

            BinaryBitmap bitmap =
                    new BinaryBitmap(
                    new HybridBinarizer(source)
                    );


            Result result =
                    new MultiFormatReader().decode(bitmap);


            return result.getText();


        } catch(NotFoundException e){

            return null;
        }
    }
}
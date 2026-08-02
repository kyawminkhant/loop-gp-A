package services;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import java.awt.image.BufferedImage;


public class WebCamService {


    private Webcam webcam;



    public void start() {


        webcam = Webcam.getDefault();


        if (webcam != null) {

            webcam.setViewSize(
                    WebcamResolution.VGA.getSize()
            );

            webcam.open();

        } else {

            throw new RuntimeException(
                    "No webcam detected"
            );

        }

    }




    public BufferedImage getFrame() {


        if (webcam != null && webcam.isOpen()) {

            return webcam.getImage();

        }


        return null;

    }





    public boolean isRunning() {


        return webcam != null
                && webcam.isOpen();

    }





    public void stop() {


        if (webcam != null && webcam.isOpen()) {

            webcam.close();

        }


        webcam = null;

    }

}
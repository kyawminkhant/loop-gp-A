package services;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ImageSaveService {


    public String save(
            BufferedImage image,
            String code
    ){

        try{

            File folder =
                    new File("saved_scans");


            if(!folder.exists())
                folder.mkdirs();



            String time =
            LocalDateTime.now()
            .format(
            DateTimeFormatter.ofPattern(
            "yyyyMMdd_HHmmss")
            );


            File file =
            new File(
            folder,
            "scan_"+code+"_"+time+".png"
            );


            ImageIO.write(
            image,
            "png",
            file
            );


            return file.getAbsolutePath();


        }catch(Exception e){

            e.printStackTrace();
            return null;
        }
    }
}
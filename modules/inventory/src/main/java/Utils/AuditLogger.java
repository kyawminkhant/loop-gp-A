package Utils;


import dao.TransactionLogDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class AuditLogger {



    public static void log(
            String action,
            String details
    ){


        String username = "Unknown";


        if(Session.getUser() != null){

            username =
            Session.getUser().getUsername();

        }



        String dateTime =
                LocalDateTime.now()
                .format(
                    DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
                    )
                );



        TransactionLogDAO.insertLog(
                username,
                action,
                details,
                dateTime
        );


    }


}
package outerpark;

import outerpark.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired NoticeRepository noticeRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_SendConfirmMsg(@Payload PaymentApproved paymentApproved){

        if(!paymentApproved.validate()) return;

        System.out.println("\n\n##### listener SendConfirmMsg : " + paymentApproved.toJson() + "\n\n");

        // Sample Logic //
        Notice notice = new Notice();
        notice.setPaymentId(paymentApproved.getReservationId());
        notice.setSendText(paymentApproved.getStatus());
        noticeRepository.save(notice);
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCancelled_SendCancelMsg(@Payload PaymentCancelled paymentCancelled){

        if(!paymentCancelled.validate()) return;

        System.out.println("\n\n##### listener SendCancelMsg : " + paymentCancelled.toJson() + "\n\n");

        // Sample Logic //
        Notice notice = new Notice();
        notice.setPaymentId(paymentCancelled.getReservationId());
        notice.setSendText(paymentCancelled.getStatus());
        noticeRepository.save(notice);
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}

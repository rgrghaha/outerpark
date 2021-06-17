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
    @Autowired PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReserved_ApprovePayment(@Payload Reserved reserved){

        //if(!reserved.validate()) return;

        if (reserved.validate()) {
            System.out.println("\n\n##### listener ApprovePayment : " + reserved.toJson() + "\n\n");

            // Sample Logic //
            Payment payment = new Payment();
            payment.setReservationId(reserved.getId());
            //payment.setPrice(reserved.getPrice());
            payment.setStatus("PaymentApproved");
            paymentRepository.save(payment);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCanceled_CancelPayment(@Payload Canceled canceled){

        //if(!canceled.validate()) return;

        if(canceled.validate()) {
            System.out.println("\n\n##### listener CancelPayment : " + canceled.toJson() + "\n\n");

            // Sample Logic //
            Payment payment = paymentRepository.findByReservationId(canceled.getId());
            //payment.setPrice(canceled.getPrice());
            payment.setStatus("PaymentCanceled");
            paymentRepository.delete(payment);
        }
  
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}

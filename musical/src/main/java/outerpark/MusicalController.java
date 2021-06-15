package outerpark;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class MusicalController {
        @Autowired
        MusicalRepository musicalRepository;


        @RequestMapping(value = "/chkAndModifySeat",
                method = RequestMethod.GET,
                produces = "application/json;charset=UTF-8")

        public boolean modifySeat(HttpServletRequest request, HttpServletResponse response) throws Exception {
                System.out.println("##### /musical/modifySeat  called #####");
                
                boolean status = false;
                Long musicalId = Long.valueOf(request.getParameter("musicalId"));
                int seats = Integer.parseInt(request.getParameter("seats"));

                Musical musical = musicalRepository.findByMusicalId(musicalId);

                if(musical.getReservableSeat() >= seats) {
                        status = true;
                        musical.setReservableSeat(musical.getReservableSeat() - seats);
                        musicalRepository.save(musical);

                }
                return status;
        }


 }

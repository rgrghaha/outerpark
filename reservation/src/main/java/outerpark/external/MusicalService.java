
package outerpark.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@FeignClient(name="musical", url="http://localhost:8081")
public interface MusicalService {

    // @RequestMapping(method= RequestMethod.GET, path="/musicals")
    // public void modifySeat(@RequestBody Musical musical);
    @RequestMapping(method= RequestMethod.GET, path="/chkAndModifySeat")
    public boolean modifySeat(@RequestParam("musicalId") String musicalId,
                              @RequestParam("seats") int seatCount);
}
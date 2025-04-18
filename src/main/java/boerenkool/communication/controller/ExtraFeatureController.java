package boerenkool.communication.controller;

import boerenkool.business.model.ExtraFeature;
import boerenkool.business.model.HouseExtraFeature;
import boerenkool.business.service.ExtraFeatureService;
import boerenkool.business.service.HouseExtraFeatureService;
import boerenkool.communication.dto.HouseExtraFeatureDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/extraFeatures")
public class ExtraFeatureController {

    private final Logger logger = LoggerFactory.getLogger(ExtraFeatureController.class);
    private final ExtraFeatureService extraFeatureService;
    private final HouseExtraFeatureService houseExtraFeatureService;

    @Autowired
    public ExtraFeatureController(ExtraFeatureService extraFeatureService, HouseExtraFeatureService houseExtraFeatureService) {
        this.extraFeatureService = extraFeatureService;
        this.houseExtraFeatureService = houseExtraFeatureService;
        logger.info("Nieuwe ExtraFeatureController aangemaakt");
    }


    @GetMapping
    public List<ExtraFeature> getAllExtraFeatures() {
        logger.info("Alle extra features ophalen");
        return extraFeatureService.getAllExtraFeatures();
    }

    @GetMapping(value = "/name/{name}")
    public ResponseEntity<?> findExtraFeatureByName(@PathVariable String name) {
        logger.info("Extra feature ophalen met naam: {}", name);
        Optional<ExtraFeature> extraFeature = extraFeatureService.findExtraFeatureByName(name);
        if (extraFeature.isPresent()) {
            return new ResponseEntity<>(extraFeature.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("ExtraFeature niet gevonden", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/houses/{houseId}")
    public ResponseEntity<List<HouseExtraFeature>> getAllFeaturesByHouseIdWithNames(@PathVariable int houseId) {
        List<HouseExtraFeature> extraFeatures = houseExtraFeatureService.getAllFeaturesByHouseIdWithNames(houseId);
        if (!extraFeatures.isEmpty()) {
            return new ResponseEntity<>(extraFeatures, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getExtraFeatureById(@PathVariable int id) {
        logger.info("Extra feature ophalen met ID: {}", id);
        Optional<ExtraFeature> extraFeature = extraFeatureService.getExtraFeatureById(id);
        if (extraFeature.isPresent()) {
            return new ResponseEntity<>(extraFeature.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("ExtraFeature niet gevonden", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<String> createExtraFeature(@RequestBody ExtraFeature extraFeature) {
        try {
            logger.info("Nieuwe extra feature aanmaken: {}", extraFeature.getExtraFeatureName());
            ExtraFeature savedExtraFeature = extraFeatureService.saveExtraFeature(extraFeature);
            return new ResponseEntity<>("ExtraFeature succesvol aangemaakt met ID: " + savedExtraFeature.getExtraFeatureId(), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("Fout bij het aanmaken van extra feature: {}", e.getMessage());
            return new ResponseEntity<>("Fout bij het aanmaken van ExtraFeature: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/houses/{houseId}/features")
    public ResponseEntity<String> saveHouseExtraFeatures(
            @PathVariable int houseId,
            @RequestBody List<HouseExtraFeatureDTO> extraFeatures) {

        try {
            List<HouseExtraFeature> houseExtraFeatures = extraFeatures.stream()
                    .map(dto -> new HouseExtraFeature(houseId, dto.getExtraFeatureId()))
                    .collect(Collectors.toList());

            houseExtraFeatureService.saveAllHouseExtraFeatures(houseExtraFeatures);

            return new ResponseEntity<>("Extra features successfully saved.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to save extra features.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PutMapping(value = "/{id}")
    public ResponseEntity<?> updateExtraFeature(@PathVariable int id, @RequestBody ExtraFeature extraFeature) {
        logger.info("Extra feature bijwerken met ID: {}", id);
        extraFeature.setExtraFeatureId(id);
        boolean updated = extraFeatureService.updateExtraFeature(extraFeature);
        if (updated) {
            return new ResponseEntity<>("ExtraFeature succesvol bijgewerkt", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Mislukt om ExtraFeature bij te werken", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/houses/{houseId}/features")
    public ResponseEntity<String> updateHouseExtraFeatures(
            @PathVariable int houseId,
            @RequestBody List<HouseExtraFeatureDTO> extraFeatures) {

        try {

            List<HouseExtraFeature> houseExtraFeatures = extraFeatures.stream()
                    .map(dto -> new HouseExtraFeature(houseId, dto.getExtraFeatureId()))
                    .collect(Collectors.toList());

            houseExtraFeatureService.removeAllExtraFeaturesFromHouse(houseId);

            houseExtraFeatureService.saveAllHouseExtraFeatures(houseExtraFeatures);

            return new ResponseEntity<>("Extra features successfully updated.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update extra features.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteExtraFeature(@PathVariable int id) {
        logger.info("Extra feature verwijderen met ID: {}", id);
        boolean deleted = extraFeatureService.deleteExtraFeatureById(id);
        if (deleted) {
            return new ResponseEntity<>("ExtraFeature succesvol verwijderd", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Mislukt om ExtraFeature te verwijderen", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}

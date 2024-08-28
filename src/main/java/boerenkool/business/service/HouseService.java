package boerenkool.business.service;

import boerenkool.business.model.House;
import boerenkool.business.model.HouseFilter;
import boerenkool.business.model.HouseType;
import boerenkool.communication.dto.HouseListDTO;
import boerenkool.database.repository.HouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


/**
 * @author Danny KWANT
 * @project Boerenkool
 * @created 13/08/2024 - 12:23
 */
@Service
public class HouseService {

    private CacheManager cacheManager;
    private final Logger logger = LoggerFactory.getLogger(HouseService.class);
    private final HouseRepository houseRepository;
    private final UserService userService;

    @Autowired
    public HouseService(HouseRepository houseRepository, CacheManager cacheManager, UserService userService) {
        this.houseRepository = houseRepository;
        this.cacheManager = cacheManager;
        this.userService = userService;
        logger.info("New HouseService");
    }

    @Cacheable(value = "houses", key = "#houseId")
    public House getOneById(int houseId) {
        return houseRepository.getHouseById(houseId).orElse(null);
    }

    public List<House> getAllHouses() {
        return houseRepository.getListOfAllHouses();
    }

    public List<House> getListOfHousesByOwnerId(int houseOwnerId) {
        return houseRepository.getListOfAllHousesByOwner(houseOwnerId);
    }

    public String getHouseOwnerName(int houseOwnerId) {
        return userService.getOneById(houseOwnerId).get().getUsername();
    }

    public List<HouseListDTO> getFilteredListOfHouses(HouseFilter filter) {
        List<House> filteredHouses = houseRepository.getHousesWithFilter(filter);
        List<HouseListDTO> strippedFilteredHouses = new ArrayList<>();

        for (House fullHouse : filteredHouses) {
            HouseListDTO strippedHouse = new HouseListDTO();
            strippedHouse.setHouseId(fullHouse.getHouseId());
            if (!fullHouse.getPictures().isEmpty()) {
                // TODO need Base64 String and MIME-Type (e.g. png/jpeg)
//                strippedHouse.setPicture(fullHouse.getPictures().get(0).getPicture());
            }
            strippedHouse.setHouseName(fullHouse.getHouseName());
            strippedHouse.setHouseType(fullHouse.getHouseType().getHouseTypeName());
            strippedHouse.setProvince(fullHouse.getProvince());
            strippedHouse.setCity(fullHouse.getCity());
            strippedHouse.setPrice(fullHouse.getPricePPPD());
            strippedFilteredHouses.add(strippedHouse);
        }
        return strippedFilteredHouses;
    }

    public List<String> getUniqueCities() {
        return houseRepository.getUniqueCities();
    }

    public List<HouseType> getAllHouseTypes() {
        return houseRepository.getAllHouseTypes();
    }

    public boolean saveHouse(House house) {
        boolean result = houseRepository.saveHouse(house);
        if (result) {
            updateCache(house);
        }
        return result;
    }

    public void updateCache(House house) {
        Cache cache = cacheManager.getCache("houses");
        if (cache != null) {
            cache.put(house.getHouseId(), house);
        }
    }

    @CacheEvict(value = "houses", key = "#houseId")
    public boolean deleteHouse(int houseId) {
        return houseRepository.deleteHouse(houseId);
    }

}

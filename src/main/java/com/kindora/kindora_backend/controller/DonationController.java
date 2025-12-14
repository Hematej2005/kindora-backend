package com.kindora.kindora_backend.controller;

import com.kindora.kindora_backend.dto.DonationRequestDTO;
import com.kindora.kindora_backend.dto.DonationResponseDTO;
import com.kindora.kindora_backend.model.Donation;
import com.kindora.kindora_backend.service.DonationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    /**
     * Accepts:
     *  - a JSON part named "data" (DonationRequestDTO) + optional image part "image"
     *  OR
     *  - form fields (multipart/form-data) produced by your HTML pages.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createDonation(
            @RequestPart(value = "data", required = false) DonationRequestDTO data,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpServletRequest request
    ) {
        try {
            if (data == null) {
                DonationRequestDTO dto = new DonationRequestDTO();

                dto.setType(firstNonNull(
                        request.getParameter("type"),
                        request.getParameter("donationType"),
                        request.getParameter("donation_type")
                ));
                dto.setDescription(firstNonNull(
                        request.getParameter("description"),
                        request.getParameter("desc")
                ));

                String totalCountStr = firstNonNull(request.getParameter("total_count"), request.getParameter("totalCount"));
                if (totalCountStr != null && !totalCountStr.isBlank()) {
                    try { dto.setTotalCount(Integer.valueOf(totalCountStr)); } catch (Exception ignored){}
                }

                String clothGroups = firstNonNull(request.getParameter("cloth_groups"), request.getParameter("clothGroups"));
                if (clothGroups == null) {
                    String[] groups = request.getParameterValues("cloth_group[]");
                    if (groups != null) clothGroups = String.join(",", groups);
                }
                if (clothGroups != null) dto.setClothGroups(Arrays.asList(clothGroups.split(",")));

                dto.setAnimalSuitableFor(firstNonNull(request.getParameter("animal_suitable_for"), request.getParameter("animals")));
                dto.setAnimalFoodType(firstNonNull(request.getParameter("animal_food_type"), request.getParameter("foodType")));

                dto.setPinCode(firstNonNull(request.getParameter("pin_code"), request.getParameter("pincode")));
                dto.setStreet(request.getParameter("street"));
                dto.setDistrict(request.getParameter("district"));
                dto.setState(request.getParameter("state"));
                dto.setLandmark(request.getParameter("landmark"));

                dto.setAvailableFrom(firstNonNull(request.getParameter("available_from"), request.getParameter("availableFrom"), request.getParameter("fromTime")));
                dto.setAvailableTo(firstNonNull(request.getParameter("available_to"), request.getParameter("availableTo"), request.getParameter("toTime")));

                try { dto.setLat(request.getParameter("lat") != null ? Double.valueOf(request.getParameter("lat")) : null); } catch (Exception ignored) {}
                try { dto.setLng(request.getParameter("lng") != null ? Double.valueOf(request.getParameter("lng")) : null); } catch (Exception ignored) {}

                // Items: robust extraction
                List<DonationRequestDTO.ItemDTO> items = new ArrayList<>();
                String[] names = request.getParameterValues("item_name[]");
                if (names == null) names = request.getParameterValues("itemName[]");
                String[] qtys = request.getParameterValues("quantity[]");
                String[] conds = request.getParameterValues("item_condition[]");
                if (conds == null) conds = request.getParameterValues("itemCondition[]");
                String[] ages = request.getParameterValues("age_group[]");
                if (ages == null) ages = request.getParameterValues("ageGroup[]");
                String[] serves = request.getParameterValues("servesFor[]");
                if (serves == null) serves = request.getParameterValues("serves_for[]");

                int max = 0;
                if (names != null) max = Math.max(max, names.length);
                if (qtys != null) max = Math.max(max, qtys.length);
                if (conds != null) max = Math.max(max, conds.length);
                if (ages != null) max = Math.max(max, ages.length);
                if (serves != null) max = Math.max(max, serves.length);

                for (int i = 0; i < max; i++) {
                    DonationRequestDTO.ItemDTO it = new DonationRequestDTO.ItemDTO();
                    if (names != null && names.length > i) it.setItemName(names[i]);
                    if (qtys != null && qtys.length > i) it.setQuantity(qtys[i]);
                    if (serves != null && serves.length > i) {
                        try { it.setServesFor(Integer.valueOf(serves[i])); } catch (Exception ignored) {}
                    }
                    if (conds != null && conds.length > i) it.setItemCondition(conds[i]);
                    if (ages != null && ages.length > i) it.setAgeGroup(ages[i]);
                    if ((it.getItemName() != null && !it.getItemName().isBlank()) || (it.getQuantity() != null && !it.getQuantity().isBlank()) || it.getServesFor() != null) {
                        items.add(it);
                    }
                }
                dto.setItems(items);

                data = dto;
            }

            Donation created = donationService.createDonation(data, image);

            DonationResponseDTO resp = new DonationResponseDTO();
            resp.setId(created.getId());
            resp.setDonorId(created.getDonorId());
            resp.setType(created.getType());
            resp.setPinCode(created.getPinCode());
            resp.setStreet(created.getStreet());
            resp.setDistrict(created.getDistrict());
            resp.setState(created.getState());
            resp.setCreatedAt(created.getCreatedAt());

            return ResponseEntity.created(URI.create("/api/donations/" + created.getId())).body(resp);

        } catch (IllegalStateException authEx) {
            return ResponseEntity.status(401).body("Unauthorized");
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Server error: " + ex.getMessage());
        }
    }

    private static String firstNonNull(String... vals) {
        if (vals == null) return null;
        for (String s : vals) if (s != null && !s.isBlank()) return s;
        return null;
    }
}

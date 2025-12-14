package com.kindora.kindora_backend.service;

import com.kindora.kindora_backend.dto.DonationRequestDTO;
import com.kindora.kindora_backend.model.Donation;
import com.kindora.kindora_backend.model.DonationImage;
import com.kindora.kindora_backend.model.DonationItem;
import com.kindora.kindora_backend.repository.DonationImageRepository;
import com.kindora.kindora_backend.repository.DonationItemRepository;
import com.kindora.kindora_backend.repository.DonationRepository;
import com.kindora.kindora_backend.util.AuthUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonationItemRepository itemRepository;
    private final DonationImageRepository imageRepository;
    private final FileStorageService fileStorageService;

    public DonationService(
            DonationRepository donationRepository,
            DonationItemRepository itemRepository,
            DonationImageRepository imageRepository,
            FileStorageService fileStorageService
    ) {
        this.donationRepository = donationRepository;
        this.itemRepository = itemRepository;
        this.imageRepository = imageRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Create donation from DTO and optional image file.
     */
    @Transactional
    public Donation createDonation(DonationRequestDTO dto, MultipartFile imageFile) throws Exception {
        Long donorId = AuthUtil.getCurrentUserId();
        if (donorId == null) throw new IllegalStateException("Unauthorized");

        Donation d = new Donation();
        d.setDonorId(donorId);

        // Type (store uppercase to match ENUM if DB expects uppercase)
        if (dto.getType() != null) d.setType(dto.getType().toUpperCase());

        // Subtypes (list -> CSV)
        if (dto.getSubTypes() != null && !dto.getSubTypes().isEmpty()) {
            d.setSubTypes(String.join(",", dto.getSubTypes()));
        }

        d.setDescription(dto.getDescription());
        d.setTotalCount(dto.getTotalCount());
        if (dto.getClothGroups() != null && !dto.getClothGroups().isEmpty()) {
            d.setClothGroups(String.join(",", dto.getClothGroups()));
        }
        d.setAnimalSuitableFor(dto.getAnimalSuitableFor());
        d.setAnimalFoodType(dto.getAnimalFoodType());

        d.setPinCode(dto.getPinCode());
        d.setStreet(dto.getStreet());
        d.setDistrict(dto.getDistrict());
        d.setState(dto.getState());
        d.setLandmark(dto.getLandmark());

        Instant from = parseToInstantFlexible(dto.getAvailableFrom());
        Instant to = parseToInstantFlexible(dto.getAvailableTo());
        if (from == null || to == null) {
            // If missing, set safe defaults (frontend ideally supplies values)
            from = Instant.now();
            to = Instant.now().plus(Duration.ofHours(6));
        }
        d.setAvailableFrom(from);
        d.setAvailableTo(to);

        d.setLat(dto.getLat());
        d.setLng(dto.getLng());

        // persist donation (insert)
        Donation saved = donationRepository.save(d);

        // items
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (DonationRequestDTO.ItemDTO it : dto.getItems()) {
                if (it == null) continue;

                boolean emptyItem = (it.getItemName() == null || it.getItemName().trim().isEmpty())
                        && (it.getQuantity() == null || it.getQuantity().toString().trim().isEmpty())
                        && it.getServesFor() == null;

                if (emptyItem) continue;

                DonationItem item = new DonationItem();
                item.setDonationId(saved.getId());
                item.setItemName(it.getItemName());
                item.setQuantity(it.getQuantity());
                item.setServesFor(it.getServesFor());

                // optional fields - set if setters exist
                try { item.setItemCondition(it.getItemCondition()); } catch (Exception ignored) {}
                try { item.setAgeGroup(it.getAgeGroup()); } catch (Exception ignored) {}

                itemRepository.save(item);
            }
        }

        // image file (store + save record)
        if (imageFile != null && !imageFile.isEmpty()) {
            String url = fileStorageService.store(imageFile);
            DonationImage img = new DonationImage();
            img.setDonationId(saved.getId());
            img.setImageUrl(url);
            imageRepository.save(img);
        }

        return saved;
    }

    /**
     * Accepts flexible formats:
     * - ISO offset (2025-12-09T14:30:00Z)
     * - Local date-time ISO (2025-12-09T14:30:00)
     * - Just HH:mm -> treat as today's time (UTC)
     * - "yyyy-MM-dd HH:mm:ss"
     */
    private Instant parseToInstantFlexible(String s) {
        if (s == null || s.isBlank()) return null;
        String trimmed = s.trim();
        try {
            // HH:mm -> today's date at that time (UTC)
            if (trimmed.matches("^\\d{2}:\\d{2}$")) {
                LocalTime t = LocalTime.parse(trimmed);
                LocalDate today = LocalDate.now(ZoneOffset.UTC);
                LocalDateTime ldt = LocalDateTime.of(today, t);
                return ldt.toInstant(ZoneOffset.UTC);
            }

            // try OffsetDateTime (preferred)
            try {
                OffsetDateTime odt = OffsetDateTime.parse(trimmed);
                return odt.toInstant();
            } catch (Exception ignored2) {}

            // try LocalDateTime ISO_LOCAL_DATE_TIME
            try {
                LocalDateTime ldt = LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return ldt.toInstant(ZoneOffset.UTC);
            } catch (Exception ignored3) {}

            // try "yyyy-MM-dd HH:mm:ss"
            try {
                DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime ldt = LocalDateTime.parse(trimmed, f);
                return ldt.toInstant(ZoneOffset.UTC);
            } catch (Exception ignored4) {}

        } catch (Exception ignored) {}
        return null;
    }
}

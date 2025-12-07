package com.dropshipping.backend.service;

import com.dropshipping.backend.entity.OrderItem;
import com.dropshipping.backend.entity.Supplier;
import com.dropshipping.backend.entity.SupplierReceipt;
import com.dropshipping.backend.repository.OrderItemRepository;
import com.dropshipping.backend.repository.SupplierReceiptRepository;
import com.dropshipping.backend.repository.SupplierRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;
import software.amazon.awssdk.services.textract.model.S3Object;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierReceiptService {

    private final SupplierRepository supplierRepository;
    private final OrderItemRepository orderItemRepository;
    private final SupplierReceiptRepository supplierReceiptRepository;

    private final S3Client s3Client;
    private final TextractClient textractClient;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.receipt-prefix:supplier-receipts/}")
    private String receiptPrefix;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SupplierReceipt uploadReceipt(Long supplierId,
                                         Long orderItemId,
                                         String receiptNumber,
                                         BigDecimal amount,
                                         String currency,
                                         LocalDate receiptDate,
                                         MultipartFile file) throws Exception {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NoSuchElementException("Supplier not found: " + supplierId));

        OrderItem orderItem = null;
        if (orderItemId != null) {
            orderItem = orderItemRepository.findById(orderItemId)
                    .orElseThrow(() -> new NoSuchElementException("Order item not found: " + orderItemId));
        }

        String key = buildS3Key(supplier, receiptDate, receiptNumber, file.getOriginalFilename());

        // Upload to S3 (public-read can be adjusted; keeping private by default - comment out ACL if undesired)
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(Optional.ofNullable(file.getContentType()).orElse("application/octet-stream"))
                .acl(ObjectCannedACL.BUCKET_OWNER_FULL_CONTROL)
                .metadata(Map.of(
                        "supplierId", String.valueOf(supplierId),
                        "receiptNumber", StringUtils.defaultString(receiptNumber, ""),
                        "orderItemId", orderItem != null ? String.valueOf(orderItem.getId()) : ""))
                .build();

        s3Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));

        // Build https URL (works for public buckets or presign-required; here only stored for reference)
        String s3Url = "s3://" + bucketName + "/" + key;

        SupplierReceipt receipt = new SupplierReceipt();
        receipt.setSupplier(supplier);
        receipt.setOrderItem(orderItem);
        receipt.setReceiptNumber(receiptNumber != null ? receiptNumber : "AUTO-" + System.currentTimeMillis());
        receipt.setAmount(amount != null ? amount : BigDecimal.ZERO);
        receipt.setCurrency(StringUtils.isNotBlank(currency) ? currency.toUpperCase(Locale.ROOT) : "USD");
        receipt.setS3Url(s3Url);
        receipt.setReceiptDate(receiptDate != null ? receiptDate : LocalDate.now());

        // Lightweight OCR for images only (skip PDFs due to async Textract job complexity)
        if (isImage(file.getOriginalFilename())) {
            try {
                ObjectNode ocr = runTextractDetect(bucketName, key);
                receipt.setOcrData(ocr);
            } catch (Exception ex) {
                log.warn("OCR failed for {}: {}", key, ex.getMessage());
            }
        }

        return supplierReceiptRepository.save(receipt);
    }

    public List<SupplierReceipt> listReceiptsForSupplier(Long supplierId, LocalDate start, LocalDate end) {
        if (start != null && end != null) {
            return supplierReceiptRepository.findAllBySupplier_IdAndReceiptDateBetween(supplierId, start, end);
        }
        return supplierRepository.findById(supplierId)
                .map(supplierReceiptRepository::findAllBySupplier)
                .orElse(List.of());
    }

    public List<SupplierReceipt> listReceiptsForOrderItem(Long orderItemId) {
        OrderItem oi = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NoSuchElementException("Order item not found: " + orderItemId));
        return supplierReceiptRepository.findAllByOrderItem(oi);
    }

    private String buildS3Key(Supplier supplier, LocalDate date, String receiptNumber, String originalName) throws Exception {
        String safeSupplier = supplier.getName().replaceAll("[^a-zA-Z0-9-_\\.]", "-");
        LocalDate d = date != null ? date : LocalDate.now();
        String yyyy = String.valueOf(d.getYear());
        String mm = String.format("%02d", d.getMonthValue());
        String dd = String.format("%02d", d.getDayOfMonth());

        String ext = guessExtension(originalName);
        String baseName = StringUtils.isNotBlank(receiptNumber) ? receiptNumber : "receipt-" + System.currentTimeMillis();
        baseName = baseName.replaceAll("[^a-zA-Z0-9-_\\.]", "-");

        String prefix = receiptPrefix;
        if (!prefix.endsWith("/")) prefix = prefix + "/";
        return prefix + yyyy + "/" + mm + "/" + dd + "/" + safeSupplier + "/" + baseName + ext;
    }

    private static String guessExtension(String originalName) {
        if (originalName == null) return ".dat";
        String lower = originalName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return lower.substring(lower.lastIndexOf('.'));
        if (lower.endsWith(".png")) return ".png";
        if (lower.endsWith(".pdf")) return ".pdf";
        return ".dat";
    }

    private static boolean isImage(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
    }

    private ObjectNode runTextractDetect(String bucket, String key) {
        Document document = Document.builder()
                .s3Object(S3Object.builder().bucket(bucket).name(key).build())
                .build();

        DetectDocumentTextRequest req = DetectDocumentTextRequest.builder()
                .document(document)
                .build();

        DetectDocumentTextResponse resp = textractClient.detectDocumentText(req);

        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode lines = objectMapper.createArrayNode();
        for (Block b : resp.blocks()) {
            if ("LINE".equalsIgnoreCase(b.blockTypeAsString())) {
                lines.add(Optional.ofNullable(b.text()).orElse(""));
            }
        }
        root.set("lines", lines);
        root.put("blockCount", resp.blocks() != null ? resp.blocks().size() : 0);
        return root;
    }
}

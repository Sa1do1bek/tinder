package com.example.tinder.controller;

import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessageRestController {
    private final MessageService messageService;
    private final UserRepository userRepository;

//    // Conversation bo'yicha barcha xabarlarni olish
//    @GetMapping("/conversation/{username}")
//    public ResponseEntity<List<MessageResponseDTO>> getConversationMessages(
//            @PathVariable String username,
//            @RequestHeader("Authorization") String token) {
//
//        String currentUsername = extractUsernameFromToken(token);
//        List<Message> messages = messageService.getConversationMessages(currentUsername, username);
//
//        List<MessageResponseDTO> response = messages.stream()
//                .map(MessageResponseDTO::from)
//                .toList();
//
//        return ResponseEntity.ok(response);
//    }
//
//    // User bilan barcha conversationlarni olish
//    @GetMapping("/conversations")
//    public ResponseEntity<List<ConversationDTO>> getAllConversations(
//            @RequestHeader("Authorization") String token) {
//
//        String username = extractUsernameFromToken(token);
//        List<ConversationDTO> conversations = messageService.getAllConversations(username);
//
//        return ResponseEntity.ok(conversations);
//    }
//
//    // O'qilmagan xabarlar soni
//    @GetMapping("/unread-count")
//    public ResponseEntity<Map<String, Long>> getUnreadCount(
//            @RequestHeader("Authorization") String token) {
//
//        String username = extractUsernameFromToken(token);
//        long count = messageService.getUnreadMessagesCount(username);
//
//        return ResponseEntity.ok(Map.of("unreadCount", count));
//    }
//
//    // User bilan o'qilmagan xabarlar
//    @GetMapping("/unread/{username}")
//    public ResponseEntity<List<MessageResponseDTO>> getUnreadMessages(
//            @PathVariable String username,
//            @RequestHeader("Authorization") String token) {
//
//        String currentUsername = extractUsernameFromToken(token);
//        List<Message> messages = messageService.getUnreadMessages(currentUsername, username);
//
//        List<MessageResponseDTO> response = messages.stream()
//                .map(MessageResponseDTO::from)
//                .toList();
//
//        return ResponseEntity.ok(response);
//    }
//
//    // Barcha o'qilmagan xabarlarni o'qilgan deb belgilash
//    @PutMapping("/mark-all-read/{username}")
//    public ResponseEntity<Void> markAllAsRead(
//            @PathVariable String username,
//            @RequestHeader("Authorization") String token) {
//
//        String currentUsername = extractUsernameFromToken(token);
//        messageService.markAllAsRead(currentUsername, username);
//
//        return ResponseEntity.ok().build();
//    }
//
//    // Xabarni o'chirish
//    @DeleteMapping("/{messageId}")
//    public ResponseEntity<Void> deleteMessage(
//            @PathVariable Long messageId,
//            @RequestHeader("Authorization") String token) {
//
//        String username = extractUsernameFromToken(token);
//        messageService.deleteMessage(messageId, username);
//
//        return ResponseEntity.ok().build();
//    }
//
//    // Xabarni tahrirlash
//    @PutMapping("/{messageId}")
//    public ResponseEntity<MessageResponseDTO> editMessage(
//            @PathVariable Long messageId,
//            @RequestBody Map<String, String> payload,
//            @RequestHeader("Authorization") String token) {
//
//        String username = extractUsernameFromToken(token);
//        String newContent = payload.get("message");
//
//        Message updated = messageService.editMessage(messageId, username, newContent);
//        return ResponseEntity.ok(MessageResponseDTO.from(updated));
//    }
//
//    private String extractUsernameFromToken(String token) {
//        // JWT tokendan username ajratib olish
//        // Bu sizning JwtUtil classingizga bog'liq
//        return "extracted_username";
//    }
}
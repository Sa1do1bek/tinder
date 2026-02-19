package com.example.tinder.utils;

import com.example.tinder.entity.Code;
import com.example.tinder.entity.User;
import com.example.tinder.repository.CodeRepository;
import com.example.tinder.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Optional;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class Utils {
    private final JavaMailSender mailSender;
    private final CodeRepository codeRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    //test
    private final RestTemplate restTemplate;
    @Value("${application.playMobile.url}")
    private String playMobileUrl;
    @Value("${application.playMobile.username}")
    private String playMobileUsername;
    @Value("${application.playMobile.password}")
    private String playMobilePassword;

    private static final double EARTH_RADIUS_KM = 6371.0;


    public boolean checkIfWithin10Km(String lat1Str, String lon1Str, String lat2Str, String lon2Str) {
        double lat1 = Double.parseDouble(lat1Str);
        double lon1 = Double.parseDouble(lon1Str);
        double lat2 = Double.parseDouble(lat2Str);
        double lon2 = Double.parseDouble(lon2Str);

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS_KM * c;

        return distance <= 10;
    }

    public boolean checkPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.trim().isEmpty())
            return phoneNumber.matches("^[0-9]{9}$");
        else return false;
    }

    public boolean checkEmail(String email) {
        if (email == null || email.isEmpty())
            return false;
        return email.contains("@");
    }

    public String getSecretKey() {
        return jwtUtil.getSecretKeyAsString();
    }

    public String getCode() {
        Random random = new Random();
        int randomCode = 100000 + random.nextInt(900000); // Generates a number between 10000 and 99999
        return String.valueOf(randomCode);
    }

    public boolean checkCode(String email, String code, Long deviceId) {
        Optional<User> optionalUser = this.userRepository.findByEmail(email);
        return checkIsPresent(code, deviceId, optionalUser);
    }

    public boolean checkCodeByPhone(String phoneNumber, String code, Long deviceId) {
        Optional<User> optionalUser = this.userRepository.findByPhoneNumber(phoneNumber);
        return checkIsPresent(code, deviceId, optionalUser);
    }

    private boolean checkIsPresent(String code, Long deviceId, Optional<User> optionalUser) {
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional<Code> optional = this.codeRepository.findByUserIdAndDeviceId(user.getId(), deviceId);
            if (optional.isPresent()) {
                Code c = optional.get();
                return code != null && !code.trim().isEmpty() && c.getCode().equals(code);
            }
        }
        return false;
    }

    public boolean sendCodeToMail(String mail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("luviuz.com@gmail.com");
            helper.setTo(mail);
            helper.setSubject("🔐 LuVi APP – Tasdiqlash kodi");

            String htmlContent = """
                        <html>
                            <body style="margin: 0; padding: 0; background: #f4f4f4; font-family: Arial, sans-serif;">
                                <div style="max-width: 600px; margin: 40px auto; padding: 30px; background: white; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                                    <h2 style="text-align: center; color: #333;">Tasdiqlash uchun kod</h2>
                                    <p style="text-align: center; color: #666;">Quyidagi kodni ilovada tasdiqlash uchun kiriting:</p>
                                    <div style="margin: 30px auto; width: fit-content; background: linear-gradient(to right, #4facfe, #00f2fe); padding: 20px 40px; border-radius: 12px; box-shadow: 0 0 10px rgba(0,0,0,0.15);">
                                        <span style="font-size: 36px; font-weight: bold; color: white; letter-spacing: 5px;">%s</span>
                                    </div>
                                    <p style="text-align: center; color: #999; font-size: 14px;">Agar bu siz bo‘lmasangiz, bu xabarni e’tiborsiz qoldiring.</p>
                                </div>
                            </body>
                        </html>
                    """.formatted(code);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            return true;

        } catch (MessagingException e) {
            return false;
        }
    }

    //-------------test-----------
    public boolean sendCodeToPhone(String phoneNumber, String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(playMobileUsername, playMobilePassword);

        String result = restTemplate.exchange(
                playMobileUrl,
                HttpMethod.POST,
                getStringHttpEntity(phoneNumber , code ,  headers),
                String.class
        ).getBody();

        if (result != null) {
            return result.equals("Request is received");
        } else {
            return false;
        }
    }

    //-------------test-----------
    private static HttpEntity<String> getStringHttpEntity(String phoneNumber, String code, HttpHeaders headers) {

        String message = "Sizning tasdiqlash kodingiz: " + code + " Iltimos, kodni hech kimga bermang!  Tinder";

        message = "\"" + message + "\"";
        String json = """
                {
                    "messages":[
                        {
                            "recipient":998""" + phoneNumber + """
                ,
                "message-id":"abc000000001",
                "sms":{
                   "originator": "3700",
                    "content": {
                       "text":\s""" + message + """
                                  }
                             }
                        }
                   ]
                }
                """;

        System.out.println(json);
        return new HttpEntity<>(json, headers);
    }

    public boolean existUsername(String username) {
        return this.userRepository.existsByUsername(username);
    }


}

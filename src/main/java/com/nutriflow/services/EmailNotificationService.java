package com.nutriflow.services;

import com.nutriflow.entities.SubscriptionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final EmailService emailService;

    /**
     * Subscription expiration warning - 7 days remaining
     */
    public void sendSubscriptionExpirationWarning(SubscriptionEntity subscription) {
        String userEmail = subscription.getUser().getEmail();
        String userName = subscription.getUser().getFirstName();
        String endDate = subscription.getEndDate()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        String subject = "⚠️ Your NutriFlow Premium Subscription is Expiring Soon";
        String body = buildExpirationWarningEmail(userName, endDate);

        emailService.sendEmail(userEmail, subject, body);
        log.info("✅ [EMAIL] Subscription expiration warning sent: {}", userEmail);
    }

    /**
     * Subscription expired notification
     */
    public void sendSubscriptionExpiredNotification(SubscriptionEntity subscription) {
        String userEmail = subscription.getUser().getEmail();
        String userName = subscription.getUser().getFirstName();

        String subject = "❌ Your NutriFlow Premium Subscription Has Expired";
        String body = buildExpiredEmail(userName);

        emailService.sendEmail(userEmail, subject, body);
        log.info("✅ [EMAIL] Subscription expired notification sent: {}", userEmail);
    }

    /**
     * Weekly report for admin
     */
    public void sendWeeklyReportToAdmin(long activeCount, long expiredCount, long cancelledCount) {
        String subject = "📊 NutriFlow - Weekly Subscription Report";
        String body = buildWeeklyReportEmail(activeCount, expiredCount, cancelledCount);

        emailService.sendEmail("tural57535@gmail.com", subject, body);
        log.info("✅ [EMAIL] Weekly report sent to admin");
    }

    // ============== EMAIL TEMPLATES ==============

    private String buildExpirationWarningEmail(String userName, String endDate) {
        return String.format("""
                Dear %s,
                
                Your NutriFlow Premium subscription is expiring soon! ⏰
                
                📅 Subscription end date: %s
                
                Renew your subscription to keep your Premium features:
                
                ✅ Unlimited access to nutrition plans
                ✅ Direct contact with your dietitian
                ✅ Professional menu plans
                ✅ Delivery service
                
                To renew your subscription: https://nutriflow.com/subscription
                
                Best regards,
                NutriFlow Team
                """, userName, endDate);
    }

    private String buildExpiredEmail(String userName) {
        return String.format("""
                Dear %s,
                
                Your NutriFlow Premium subscription has expired. 😔
                
                Access to your Premium features has been suspended.
                
                To use premium services again, please renew your subscription:
                https://nutriflow.com/subscription
                
                Best regards,
                NutriFlow Team
                """, userName);
    }

    private String buildWeeklyReportEmail(long activeCount, long expiredCount, long cancelledCount) {
        long totalCount = activeCount + expiredCount + cancelledCount;
        return String.format("""
                📊 WEEKLY SUBSCRIPTION REPORT
                ================================
                
                ✅ Active Subscriptions: %d
                ❌ Expired Subscriptions: %d
                🚫 Cancelled: %d
                
                📈 Total: %d
                
                ---
                NutriFlow Admin Panel
                """, activeCount, expiredCount, cancelledCount, totalCount);
    }
}
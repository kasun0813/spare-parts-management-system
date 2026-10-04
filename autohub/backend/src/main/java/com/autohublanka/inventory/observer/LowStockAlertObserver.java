package com.autohublanka.inventory.observer;

import com.autohublanka.dashboard.service.SettingsService;
import com.autohublanka.inventory.entity.Inventory;
import com.autohublanka.notification.service.NotificationService;
import com.autohublanka.user.entity.Role;
import org.springframework.stereotype.Component;

/**
 * Concrete observer: when a part drops to (or below) its reorder level, the inventory manager gets a
 * "Low stock" alert. Only on the change that crosses the level, so the alert is not repeated on every sale.
 */
@Component
public class LowStockAlertObserver implements StockObserver {

    private final NotificationService notifications;
    private final SettingsService settings;

    public LowStockAlertObserver(NotificationService notifications, SettingsService settings) {
        this.notifications = notifications;
        this.settings = settings;
    }

    @Override
    public void update(Inventory inventory, int previousQuantity) {
        if (!StockChanges.justBecameLow(inventory, previousQuantity) || !settings.current().lowStockNotifications()) {
            return;
        }
        notifications.notifyRole(Role.INVENTORY_MANAGER, "Low stock",
                inventory.getSparePart().getName() + " has " + inventory.getQuantity()
                        + " unit(s) left (reorder level " + inventory.getReorderLevel() + ").",
                "/dashboard/inventory");
    }
}

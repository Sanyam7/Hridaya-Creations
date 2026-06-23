package com.hridayacreations.constants;

/**
 * Centralised, reusable response messages to keep API responses consistent.
 */
public final class MessageConstants {

    private MessageConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    /* Auth */
    public static final String REGISTER_SUCCESS = "User registered successfully";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String LOGOUT_SUCCESS = "Logout successful";
    public static final String TOKEN_REFRESH_SUCCESS = "Token refreshed successfully";
    public static final String PASSWORD_CHANGED = "Password changed successfully";
    public static final String PASSWORD_RESET_LINK_SENT = "If the email exists, a password reset link has been sent";
    public static final String PASSWORD_RESET_SUCCESS = "Password reset successfully";
    public static final String PROFILE_UPDATED = "Profile updated successfully";

    /* Category */
    public static final String CATEGORY_CREATED = "Category created successfully";
    public static final String CATEGORY_UPDATED = "Category updated successfully";
    public static final String CATEGORY_DELETED = "Category deleted successfully";
    public static final String CATEGORY_FETCHED = "Category fetched successfully";
    public static final String CATEGORIES_FETCHED = "Categories fetched successfully";

    /* Product */
    public static final String PRODUCT_CREATED = "Product created successfully";
    public static final String PRODUCT_UPDATED = "Product updated successfully";
    public static final String PRODUCT_DELETED = "Product deleted successfully";
    public static final String PRODUCT_FETCHED = "Product fetched successfully";
    public static final String PRODUCTS_FETCHED = "Products fetched successfully";
    public static final String PRODUCT_ENABLED = "Product enabled successfully";
    public static final String PRODUCT_DISABLED = "Product disabled successfully";
    public static final String PRICING_UPDATED = "Pricing updated successfully";
    public static final String INVENTORY_UPDATED = "Inventory updated successfully";

    /* Image */
    public static final String IMAGE_UPLOADED = "Image uploaded successfully";
    public static final String IMAGE_DELETED = "Image deleted successfully";
    public static final String IMAGE_REPLACED = "Image replaced successfully";

    /* Cart */
    public static final String CART_FETCHED = "Cart fetched successfully";
    public static final String CART_ITEM_ADDED = "Item added to cart successfully";
    public static final String CART_ITEM_UPDATED = "Cart item updated successfully";
    public static final String CART_ITEM_REMOVED = "Item removed from cart successfully";
    public static final String CART_CLEARED = "Cart cleared successfully";

    /* Order */
    public static final String ORDER_PLACED = "Order placed successfully";
    public static final String ORDER_CANCELLED = "Order cancelled successfully";
    public static final String ORDER_FETCHED = "Order fetched successfully";
    public static final String ORDERS_FETCHED = "Orders fetched successfully";
    public static final String ORDER_STATUS_UPDATED = "Order status updated successfully";

    /* Address */
    public static final String ADDRESS_CREATED = "Address created successfully";
    public static final String ADDRESS_UPDATED = "Address updated successfully";
    public static final String ADDRESS_DELETED = "Address deleted successfully";
    public static final String ADDRESS_FETCHED = "Address fetched successfully";
    public static final String ADDRESSES_FETCHED = "Addresses fetched successfully";

    /* Wishlist */
    public static final String WISHLIST_ITEM_ADDED = "Product added to wishlist successfully";
    public static final String WISHLIST_ITEM_REMOVED = "Product removed from wishlist successfully";
    public static final String WISHLIST_FETCHED = "Wishlist fetched successfully";

    /* Review */
    public static final String REVIEW_CREATED = "Review added successfully";
    public static final String REVIEW_UPDATED = "Review updated successfully";
    public static final String REVIEW_DELETED = "Review deleted successfully";
    public static final String REVIEWS_FETCHED = "Reviews fetched successfully";

    /* User (admin) */
    public static final String USER_FETCHED = "User fetched successfully";
    public static final String USERS_FETCHED = "Users fetched successfully";
    public static final String USER_STATUS_UPDATED = "User status updated successfully";
}

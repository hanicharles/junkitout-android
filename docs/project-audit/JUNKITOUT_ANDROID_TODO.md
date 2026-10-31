# JunkItOut Android Application — Outstanding Work & Recommendations

## Outstanding Items & Recommendations

### Critical / High Priority (P0 / P1)
- **None**. All critical launch crashes, package name mappings, release key signing configurations, network timeouts, and driver endpoints are **100% resolved and verified**.

---

### Medium Priority (P2)
1. **Firebase Push Notifications (FCM) Integration**:
   - Currently, in-app notifications and live polling updates are fully functional. Integrating FCM device token registration will allow system lock-screen push alerts when app is closed.
2. **Google Maps Live Moving Marker**:
   - Customer tracking screen currently displays collector status, distance, vehicle number, and static GPS coordinates. Animating a moving vehicle marker on the customer map view when driver streams live location (`/api/driver/location`) will further enhance UX.

---

### Low Priority / Future Enhancements (P3)
1. **Multi-Language Support (Kannada & Hindi)**:
   - App currently operates in English (India). Adding string resources in `values-kn/strings.xml` and `values-hi/strings.xml` will allow full regional language switching.
2. **Camera Photo Proof Upload**:
   - Driver weight entry currently records weighed waste kg directly in database. Adding photo proof upload for doorstep scrap collection.

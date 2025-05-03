Giải thích triển khai Rate Limiting
1. Cơ chế hoạt động
   Rate Limiting trong Spring Cloud Gateway được triển khai dựa trên Redis để theo dõi và giới hạn số lượng request. Mỗi khi một request đến, gateway sẽ:

Xác định key duy nhất cho request (dựa trên IP, người dùng, hoặc path)
Kiểm tra trong Redis xem key này đã đạt giới hạn chưa
Nếu chưa đạt giới hạn, cho phép request đi tiếp
Nếu đã đạt giới hạn, trả về mã lỗi 429 (Too Many Requests)

2. Các thành phần chính

RedisRateLimiterConfig: Cấu hình các bộ rate limiter cho từng API

replenishRate: Số lượng token được thêm vào mỗi giây (request/giây)
burstCapacity: Số lượng token tối đa có thể tích lũy (cho phép burst)


KeyResolver: Xác định cách tạo key để phân biệt các nguồn request

ipKeyResolver: Giới hạn theo địa chỉ IP
userKeyResolver: Giới hạn theo người dùng (cần xác thực)
apiPathKeyResolver: Giới hạn theo đường dẫn API


EnhancedGatewayConfig: Tích hợp Rate Limiting vào các route

.requestRateLimiter(): Áp dụng Rate Limiting cho route
.setStatusCode(): Mã HTTP trả về khi vượt quá giới hạn
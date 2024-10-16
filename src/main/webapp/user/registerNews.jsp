<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Register</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
   <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <h2 class="text-center mt-5">Đăng ký nhận bản tin</h2>
                <form action="${pageContext.request.contextPath}/user/subscribe" method="post" class="mt-4">
                    <div class="form-group">
                        <label for="email">Email của bạn:</label>
                        <input type="email" class="form-control" id="email" name="email" placeholder="Nhập email" required>
                    </div>
                    <div class="form-group">
                        <label for="topics">Chọn chủ đề quan tâm:</label>
                        <select class="form-control" id="topics" name="topics[]" multiple required>
                            <option value="technology">Công nghệ</option>
                            <option value="sports">Thể thao</option>
                            <option value="entertainment">Giải trí</option>
                            <option value="business">Kinh doanh</option>
                            <option value="health">Sức khỏe</option>
                        </select>
                        
                        <small class="form-text text-muted">Giữ Ctrl để chọn nhiều chủ đề.</small>
                    </div>
                    <button type="submit" class="btn btn-primary btn-block">Đăng ký</button>
                </form>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

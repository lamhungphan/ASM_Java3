<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:url value="/admin/article" var="url" />
<div class="container">
	<h1 class="mt-4">Tin tức</h1>
	<h2>Tác giả: ${article.authorName}</h2>
	<h2>Ngày đăng: ${article.postedDate}</h2>

	<form action="${url}" method="post" enctype="multipart/form-data">
		<div class="mb-3">
			<c:if test="${sessionScope.currUser.role=='true'}">
				<h4>Loại tin: ${article.categoryName}</h4>
			</c:if>
			<c:if test="${sessionScope.currUser.role=='false'}">
				<label for="category" class="form-label">Chọn loại tin</label>
				<select name="category" id="category" class="form-select">
					<c:forEach var="cate" items="${categories}">
						<option value="${cate.id}"
							${article.categoryId==cate.id?'selected':''}>${cate.name}</option>
					</c:forEach>
				</select>
			</c:if>
		</div>

		<div class="mb-3">
			<label for="title" class="form-label">Tiêu đề</label> <input
				name="title" type="text" id="title" class="form-control"
				value="${article.title}"
				${sessionScope.currUser.role=='true'?'readonly':''}>
		</div>

		<div class="mb-3">
			<label class="form-label">Hình ảnh</label>
			<div>
				<img alt="${article.image}" src="${pageContext.request.contextPath}/photo/${article.image}"
					class="img-fluid mb-2" id="preview"> <br> <input
					type="file" name="img" id="file" accept="image/*"
					onchange="previewImage(event)" ${sessionScope.currUser.role==true?'hidden':''}> <br>
			</div>
		</div>

		<div class="mb-3">
			<label for="content" class="form-label">Nội dung</label>
			<textarea rows="10" name="content" id="content" class="form-control"
				${sessionScope.currUser.role=='true'?'readonly':''}>${article.content}</textarea>
		</div>

		<c:if test="${sessionScope.currUser.role=='false'}">
			<div class="text-center">
				<button formaction="${url}/create" ${action=='edit'?'hidden':''}
					class="btn btn-success">Tạo</button>
				<button formaction="${url}/update" ${action=='create'?'hidden':''}
					class="btn btn-warning">Sửa</button>
				<button formaction="${url}/delete" ${action=='create'?'hidden':''}
					class="btn btn-danger">Xóa</button>
				<button formaction="${url}/reset" class="btn btn-secondary">Làm
					mới</button>
			</div>
		</c:if>
		<c:if test="${sessionScope.currUser.role=='true'}">
			<input type="checkbox" name="home">
			<label>Cho phép lên trang chủ?</label><br>
			<button formaction="${url}/delete" class="btn btn-danger">Xóa</button>
			<br>
		</c:if>
	</form>
</div>
<script>
	function previewImage(event) {
		const preview = document.getElementById('preview');
		const file = event.target.files[0];
		const reader = new FileReader();

		reader.onload = function() {
			preview.src = reader.result;
		}

		if (file) {
			reader.readAsDataURL(file);
		}
	}
</script>
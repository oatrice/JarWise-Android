# Luma Code Review Report

**Date:** 2026-03-10 11:21:05
**Files Reviewed:** ['app/src/main/java/com/oatrice/jarwise/data/model/GraphDataPointDto.kt', 'app/src/main/java/com/oatrice/jarwise/ui/managejars/ManageJarsScreen.kt', 'app/src/main/java/com/oatrice/jarwise/ui/reports/ReportsViewModel.kt', 'app/src/main/java/com/oatrice/jarwise/ui/SettingsScreen.kt', 'app/src/main/java/com/oatrice/jarwise/ui/components/ExpenseGraph.kt', 'app/src/main/java/com/oatrice/jarwise/MainActivity.kt', 'app/src/main/java/com/oatrice/jarwise/data/repository/GraphRepository.kt', 'app/src/main/java/com/oatrice/jarwise/ui/managejars/ManageJarsViewModel.kt', 'code_review.md', 'app/src/main/java/com/oatrice/jarwise/ui/components/BottomNav.kt', 'app/src/main/java/com/oatrice/jarwise/ui/reports/ReportsScreen.kt', 'app/src/main/java/com/oatrice/jarwise/data/api/GraphApi.kt', 'app/src/main/java/com/oatrice/jarwise/di/ViewModelModule.kt', 'gradle/libs.versions.toml', 'app/src/main/java/com/oatrice/jarwise/di/RepositoryModule.kt', 'app/build.gradle.kts', 'app/src/main/java/com/oatrice/jarwise/di/NetworkModule.kt']

## 📝 Reviewer Feedback

Rà soát mã nguồn cho thấy có lỗi trong tính năng Báo cáo (Reports). Biểu tượng tiền tệ được gán cứng là Baht Thái (฿), dẫn đến việc hiển thị sai cho người dùng đã chọn các loại tiền tệ khác như USD hoặc EUR.

Lỗi này nằm trong tệp `app/src/main/java/com/oatrice/jarwise/ui/reports/ReportsScreen.kt`.

Để khắc phục lỗi này, bạn cần thực hiện các thay đổi sau để sử dụng đơn vị tiền tệ do người dùng lựa chọn một cách linh động:

**1. Trong `MainActivity.kt`, truyền `selectedCurrency` vào `ReportsScreen`:**

Thay đổi lời gọi `ReportsScreen` để truyền vào trạng thái tiền tệ đã chọn.

```kotlin
// trong khối when (currentScreen)
is Screen.Reports -> com.oatrice.jarwise.ui.reports.ReportsScreen(
    onBack = { currentScreen = Screen.Dashboard },
    onNavigate = handleNavigation,
    selectedCurrency = selectedCurrency // << Thêm dòng này
)
```

**2. Cập nhật `ReportsScreen.kt` để sử dụng `selectedCurrency`:**

Sửa đổi `ReportsScreen` để chấp nhận tham số tiền tệ và sử dụng `TransactionDisplayUtils` để định dạng số tiền một cách chính xác.

```kotlin
package com.oatrice.jarwise.ui.reports

// ... các import khác
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oatrice.jarwise.ui.components.BottomNav
import com.oatrice.jarwise.ui.components.NavPage
import com.oatrice.jarwise.utils.TransactionDisplayUtils // << Thêm import này
import androidx.compose.runtime.remember // << Thêm import này

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    onNavigate: (NavPage) -> Unit,
    viewModel: ReportsViewModel = viewModel(),
    selectedCurrency: String // << Thêm tham số này
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    // Lấy biểu tượng tiền tệ một cách linh động
    val currencySymbol = remember(selectedCurrency) {
        TransactionDisplayUtils.formatCurrency(0.0, selectedCurrency).firstOrNull() ?: '$'
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(...) { paddingValues ->
            Column(...) {
                // ...
                // Cập nhật các lời gọi SummaryCard
                Row(...) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Income",
                        amount = uiState.income,
                        icon = Icons.Rounded.TrendingUp,
                        color = Color(0xFF10B981),
                        currencySymbol = currencySymbol // << Truyền biểu tượng
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Expense",
                        amount = uiState.expense,
                        icon = Icons.Rounded.TrendingDown,
                        color = Color(0xFFF43F5E),
                        currencySymbol = currencySymbol // << Truyền biểu tượng
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = "Net",
                        amount = uiState.net,
                        icon = Icons.Rounded.Wallet,
                        color = Color(0xFF6366F1),
                        currencySymbol = currencySymbol // << Truyền biểu tượng
                    )
                }

                // ...

                // Cập nhật danh sách Jar Distribution
                Card(...) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        uiState.jarData.forEach { jar ->
                            Row(...) {
                                // ...
                                Text(
                                    // Sử dụng hàm tiện ích để định dạng
                                    TransactionDisplayUtils.formatCurrency(jar.amount, selectedCurrency),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // ...
            }
        }
        // ...
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    icon: ImageVector,
    color: Color,
    currencySymbol: Char // << Thêm tham số này
) {
    Card(...) {
        Column(...) {
            // ...
            Text(
                // Sử dụng biểu tượng tiền tệ linh động
                "$currencySymbol${"%.1f".format(amount / 1000)}k",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
```

## 🧪 Test Suggestions

สวัสดีครับ ผมไม่เห็นรายละเอียดการเปลี่ยนแปลงของโค้ด (code changes) ที่จะนำมาวิเคราะห์ครับ

รบกวนช่วยส่งโค้ดส่วนที่มีการเปลี่ยนแปลง เพื่อให้ผมสร้าง "Manual Verification Guide" หรือคู่มือการตรวจสอบการทำงานด้วยตนเองให้ได้ครับ


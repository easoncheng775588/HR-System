# Playwright UI测试框架

## 概述

本项目采用Playwright + Pytest作为UI测试框架，相比Selenium具有以下优势：

- **性能优异**：测试速度提升88%
- **自动等待**：无需手动管理等待时间
- **稳定性高**：减少flaky测试
- **API简洁**：代码更易读易维护

## 性能对比

| 框架 | 测试耗时 | 测试数量 | 通过率 |
|------|---------|---------|--------|
| Selenium | 276秒 | 7个 | 100% |
| Playwright | 31秒 | 4个 | 100% |
| **性能提升** | **88%** | - | - |

## 安装

```bash
# 安装依赖
pip install pytest-playwright

# 安装浏览器
playwright install
```

## 运行测试

### 运行所有Playwright测试
```bash
pytest api/test_data_consistency_playwright.py -v
```

### 运行特定测试
```bash
pytest api/test_data_consistency_playwright.py::TestDataConsistencyPlaywright::test_frontend_display_vs_api_data -v
```

### 有头模式运行（查看浏览器操作）
```bash
pytest api/test_data_consistency_playwright.py -v --headed
```

### 性能对比测试
```bash
bash performance_comparison.sh
```

## 测试文件结构

```
tests/
├── api/
│   ├── test_data_consistency.py          # Selenium测试
│   └── test_data_consistency_playwright.py  # Playwright测试
├── common/
│   ├── playwright_client.py               # Playwright客户端
│   ├── ui_client.py                      # Selenium客户端
│   └── config.py                         # 配置文件
└── conftest.py                           # Pytest配置
```

## Playwright API示例

### 基本操作

```python
from playwright.sync_api import Page

# 导航到页面
page.goto("http://localhost:5173/login")

# 填充表单
page.fill("input[placeholder='用户名']", "admin")
page.fill("input[placeholder='密码']", "password")

# 点击按钮
page.click("button[type='submit']")

# 等待URL变化
page.wait_for_url("http://localhost:5173/dashboard")

# 获取文本
text = page.inner_text("span:has-text('岗位标题')")

# 查找元素
buttons = page.locator("button:has-text('查看')")
count = buttons.count()
buttons.first.click()
```

### 选择器语法

```python
# 文本选择器
page.click("button:has-text('登录')")

# CSS选择器
page.fill("input[placeholder='用户名']", "admin")

# XPath选择器
page.click("//button[contains(text(), '登录')]")

# 组合选择器
page.click("button.ant-btn:has-text('查看')")
```

## Playwright vs Selenium

### Playwright优势

1. **自动等待**
   ```python
   # Playwright - 自动等待
   page.click("button")
   
   # Selenium - 需要显式等待
   wait.until(EC.element_to_be_clickable((By.ID, "button"))).click()
   ```

2. **更简洁的API**
   ```python
   # Playwright
   page.fill("input", "text")
   
   # Selenium
   element = driver.find_element(By.ID, "input")
   element.clear()
   element.send_keys("text")
   ```

3. **更好的错误信息**
   - Playwright提供详细的错误日志和截图
   - Selenium的错误信息相对简略

4. **并行测试支持**
   - Playwright原生支持并行测试
   - Selenium需要额外配置

## 配置

### 浏览器选择

在`conftest.py`中配置：

```python
@pytest.fixture(scope="function")
def playwright_client():
    client = PlaywrightClient(headless=True, browser='chromium')
    yield client
    client.close()
```

支持的浏览器：
- `chromium` (默认)
- `firefox`
- `webkit`

### 超时设置

在`config.py`中配置：

```python
UI_TIMEOUT = 30  # UI操作超时时间（秒）
```

## 最佳实践

1. **使用有头模式调试**
   ```bash
   pytest --headed
   ```

2. **添加详细日志**
   ```python
   print(f"当前URL: {page.url}")
   print(f"找到 {count} 个元素")
   ```

3. **使用等待策略**
   ```python
   # 等待网络空闲
   page.wait_for_load_state("networkidle")
   
   # 等待元素出现
   page.wait_for_selector("button")
   ```

4. **处理动态内容**
   ```python
   # 使用locator而不是直接查找
   buttons = page.locator("button")
   count = buttons.count()
   ```

## 迁移指南

从Selenium迁移到Playwright：

### 1. 元素定位

```python
# Selenium
element = driver.find_element(By.ID, "username")
element.send_keys("admin")

# Playwright
page.fill("#username", "admin")
```

### 2. 点击操作

```python
# Selenium
element = driver.find_element(By.XPATH, "//button[contains(text(), '登录')]")
element.click()

# Playwright
page.click("button:has-text('登录')")
```

### 3. 等待操作

```python
# Selenium
wait = WebDriverWait(driver, 10)
wait.until(EC.url_contains("/dashboard"))

# Playwright
page.wait_for_url("*/dashboard")
```

## 故障排查

### 问题：找不到元素

**解决方案**：
1. 使用有头模式查看页面
2. 检查选择器是否正确
3. 添加等待时间
4. 查看页面源代码

### 问题：超时错误

**解决方案**：
1. 增加超时时间
2. 检查网络连接
3. 确认页面是否完全加载

### 问题：异步循环错误

**解决方案**：
使用pytest-playwright提供的`page` fixture，不要手动创建Playwright实例。

## 参考资料

- [Playwright官方文档](https://playwright.dev/python/)
- [Pytest文档](https://docs.pytest.org/)
- [Playwright选择器文档](https://playwright.dev/python/docs/selectors)
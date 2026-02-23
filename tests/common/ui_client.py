# UI客户端模块，用于处理前端UI测试

from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from .config import config
import time

class UiClient:
    def __init__(self, headless=True):
        """
        初始化UI客户端
        :param headless: 是否以无头模式运行
        """
        chrome_options = Options()
        if headless:
            chrome_options.add_argument("--headless")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")
        chrome_options.add_argument("--window-size=1920,1080")
        
        self.driver = webdriver.Chrome(options=chrome_options)
        self.wait = WebDriverWait(self.driver, config.UI_TIMEOUT)
        self.base_url = config.FRONTEND_URL
    
    def open(self, path=""):
        """
        打开指定路径的页面
        :param path: 页面路径
        """
        url = f"{self.base_url}{path}"
        self.driver.get(url)
        time.sleep(2)  # 等待页面加载
    
    def login(self, username, password):
        """
        登录前端页面
        :param username: 用户名
        :param password: 密码
        :return: 是否登录成功
        """
        try:
            self.open("/login")
            
            # 输入用户名
            username_input = self.wait.until(
                EC.presence_of_element_located((By.NAME, "username"))
            )
            username_input.send_keys(username)
            
            # 输入密码
            password_input = self.wait.until(
                EC.presence_of_element_located((By.NAME, "password"))
            )
            password_input.send_keys(password)
            
            # 点击登录按钮
            login_button = self.wait.until(
                EC.element_to_be_clickable((By.CSS_SELECTOR, "button[type='submit']"))
            )
            login_button.click()
            
            # 等待登录成功，跳转到首页
            self.wait.until(
                EC.url_contains("/")
            )
            time.sleep(2)
            return True
        except Exception as e:
            print(f"登录失败: {e}")
            return False
    
    def click(self, locator):
        """
        点击元素
        :param locator: 元素定位器 (By, value)
        """
        element = self.wait.until(EC.element_to_be_clickable(locator))
        element.click()
        time.sleep(1)
    
    def input(self, locator, text):
        """
        输入文本
        :param locator: 元素定位器 (By, value)
        :param text: 输入的文本
        """
        element = self.wait.until(EC.presence_of_element_located(locator))
        element.clear()
        element.send_keys(text)
    
    def select(self, locator, value):
        """
        选择下拉框选项
        :param locator: 元素定位器 (By, value)
        :param value: 选项值
        """
        from selenium.webdriver.support.ui import Select
        element = self.wait.until(EC.presence_of_element_located(locator))
        select = Select(element)
        select.select_by_value(value)
        time.sleep(1)
    
    def get_text(self, locator):
        """
        获取元素文本
        :param locator: 元素定位器 (By, value)
        :return: 元素文本
        """
        element = self.wait.until(EC.presence_of_element_located(locator))
        return element.text
    
    def is_element_present(self, locator):
        """
        检查元素是否存在
        :param locator: 元素定位器 (By, value)
        :return: 是否存在
        """
        try:
            self.wait.until(EC.presence_of_element_located(locator))
            return True
        except:
            return False
    
    def close(self):
        """
        关闭浏览器
        """
        if self.driver:
            self.driver.quit()

# 实例化UI客户端
ui_client = UiClient()

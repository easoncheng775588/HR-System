# UI客户端模块，用于处理前端UI测试

import os
import platform
from selenium import webdriver
from selenium.webdriver.chrome.options import Options as ChromeOptions
from selenium.webdriver.chrome.service import Service as ChromeService
from selenium.webdriver.safari.options import Options as SafariOptions
from selenium.webdriver.safari.webdriver import WebDriver as SafariWebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager
from .config import config
import time

class UiClient:
    def __init__(self, headless=True, browser='auto'):
        """
        初始化UI客户端
        :param headless: 是否以无头模式运行
        :param browser: 浏览器类型 ('auto', 'chrome', 'safari')
                      auto: 自动选择（macOS优先Safari，其他系统Chrome）
                      chrome: 强制使用Chrome
                      safari: 强制使用Safari（仅macOS）
        """
        self.browser = browser
        
        if browser == 'auto':
            if platform.system() == 'Darwin':
                self.browser = 'safari'
            else:
                self.browser = 'chrome'
        
        if self.browser == 'safari':
            self._init_safari_driver(headless)
        else:
            self._init_chrome_driver(headless)
        
        self.wait = WebDriverWait(self.driver, config.UI_TIMEOUT)
        self.short_wait = WebDriverWait(self.driver, config.UI_SHORT_TIMEOUT)
        self.base_url = config.FRONTEND_URL
    
    def _init_chrome_driver(self, headless):
        """初始化Chrome浏览器驱动"""
        chrome_options = ChromeOptions()
        if headless:
            chrome_options.add_argument("--headless")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-dev-shm-usage")
        chrome_options.add_argument("--window-size=1920,1080")
        
        # 设置webdriver-manager缓存目录到项目目录
        cache_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), ".wdm")
        os.environ['WDM_CACHE_DIR'] = cache_dir
        
        try:
            service = ChromeService(ChromeDriverManager().install())
            self.driver = webdriver.Chrome(service=service, options=chrome_options)
        except Exception as e:
            print(f"Chrome浏览器初始化失败: {e}")
            if platform.system() == 'Darwin':
                print("尝试使用Safari浏览器...")
                self.browser = 'safari'
                self._init_safari_driver(headless)
            else:
                raise
    
    def _init_safari_driver(self, headless):
        """初始化Safari浏览器驱动（仅macOS）"""
        if platform.system() != 'Darwin':
            raise RuntimeError("Safari浏览器仅在macOS系统上可用")
        
        # 尝试清理旧的Safari WebDriver会话
        try:
            import subprocess
            subprocess.run(['killall', '-9', 'safaridriver'], 
                         stdout=subprocess.DEVNULL, 
                         stderr=subprocess.DEVNULL,
                         timeout=2)
        except:
            pass
        
        safari_options = SafariOptions()
        
        # Safari不支持无头模式，但在macOS上可以后台运行
        if headless:
            print("注意: Safari不支持无头模式，将在后台运行")
        
        try:
            self.driver = SafariWebDriver(options=safari_options)
            self.driver.set_window_size(1920, 1080)
        except Exception as e:
            error_msg = str(e)
            if 'Allow remote automation' in error_msg:
                raise RuntimeError(
                    "Safari的'允许远程自动化'选项未启用。\n"
                    "请按以下步骤启用：\n"
                    "  1. 打开Safari浏览器\n"
                    "  2. Safari > 偏好设置 > 高级\n"
                    "  3. 勾选'在菜单栏中显示开发菜单'\n"
                    "  4. 开发 > 允许远程自动化"
                )
            elif 'timed out' in error_msg or 'timeout' in error_msg:
                raise RuntimeError(
                    "Safari浏览器连接超时。\n"
                    "可能的原因：\n"
                    "  1. Safari浏览器未打开\n"
                    "  2. Safari WebDriver权限不足\n"
                    "  3. 系统资源不足\n"
                    "建议：跳过UI测试或使用Chrome浏览器"
                )
            elif 'already paired' in error_msg:
                raise RuntimeError(
                    "Safari浏览器已经与另一个WebDriver会话配对。\n"
                    "请尝试：\n"
                    "  1. 重启Safari浏览器\n"
                    "  2. 运行命令: killall -9 safaridriver\n"
                    "  3. 然后重新运行测试"
                )
            else:
                print(f"Safari浏览器初始化失败: {e}")
                raise
    
    def open(self, path):
        """
        打开指定路径的页面
        :param path: 页面路径
        """
        url = f"{self.base_url}{path}"
        self.driver.get(url)
        # 等待页面加载完成
        try:
            self.wait.until(
                lambda driver: driver.execute_script("return document.readyState") == "complete"
            )
        except:
            pass
    
    def login(self, username, password):
        """
        登录前端页面
        :param username: 用户名
        :param password: 密码
        :return: 是否登录成功
        """
        try:
            print(f"正在打开登录页面...")
            self.open("/login")
            print(f"登录页面已打开，当前URL: {self.driver.current_url}")
            
            # 输入用户名 - 使用placeholder来定位
            print(f"正在查找用户名输入框...")
            username_input = self.short_wait.until(
                EC.presence_of_element_located((By.XPATH, "//input[@placeholder='用户名']"))
            )
            print(f"找到用户名输入框，正在输入用户名: {username}")
            username_input.clear()
            username_input.send_keys(username)
            
            # 输入密码 - 使用placeholder来定位
            print(f"正在查找密码输入框...")
            password_input = self.short_wait.until(
                EC.presence_of_element_located((By.XPATH, "//input[@placeholder='密码']"))
            )
            print(f"找到密码输入框，正在输入密码...")
            password_input.clear()
            password_input.send_keys(password)
            
            # 点击登录按钮
            print(f"正在查找登录按钮...")
            # 尝试多种方式找到登录按钮
            login_button = None
            try:
                login_button = self.short_wait.until(
                    EC.element_to_be_clickable((By.XPATH, "//button[contains(text(),'登录')]"))
                )
                print(f"方式1找到登录按钮")
            except:
                try:
                    login_button = self.driver.find_element(By.CSS_SELECTOR, "button[type='submit']")
                    print(f"方式2找到登录按钮")
                except:
                    try:
                        login_button = self.driver.find_element(By.XPATH, "//button[@type='submit']")
                        print(f"方式3找到登录按钮")
                    except:
                        try:
                            # 尝试通过class查找
                            login_button = self.driver.find_element(By.CSS_SELECTOR, "button.ant-btn-primary")
                            print(f"方式4找到登录按钮")
                        except:
                            pass
            
            if login_button:
                print(f"找到登录按钮，正在点击...")
                login_button.click()
            else:
                print(f"未找到登录按钮，使用JavaScript提交表单...")
                # 使用JavaScript提交表单
                self.driver.execute_script("document.querySelector('form').submit()")
            
            # 等待登录成功，跳转到首页
            print(f"等待跳转到dashboard...")
            try:
                self.wait.until(
                    EC.url_contains("/dashboard")
                )
                print(f"登录成功，当前URL: {self.driver.current_url}")
                return True
            except:
                print(f"登录失败，当前URL: {self.driver.current_url}")
                # 打印页面信息用于调试
                print(f"页面标题: {self.driver.title}")
                print(f"页面URL: {self.driver.current_url}")
                # 打印错误信息
                try:
                    error_messages = self.driver.find_elements(By.CLASS_NAME, "ant-message-error")
                    for msg in error_messages:
                        print(f"错误信息: {msg.text}")
                except:
                    pass
                return False
        except Exception as e:
            print(f"登录失败: {e}")
            print(f"当前URL: {self.driver.current_url if hasattr(self, 'driver') else 'N/A'}")
            import traceback
            traceback.print_exc()
            return False
    
    def click(self, locator):
        """
        点击元素
        :param locator: 元素定位器 (By, value)
        """
        try:
            element = self.wait.until(EC.element_to_be_clickable(locator))
            element.click()
            # 点击后等待页面响应
            try:
                self.wait.until(
                    lambda driver: driver.execute_script("return document.readyState") == "complete"
                )
            except:
                pass
        except Exception as e:
            print(f"点击元素失败: {e}")
            print(f"定位器: {locator}")
            raise
    
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
    
    def get_recruitment_request_detail(self, request_id):
        """
        获取用人申请详情的前端显示数据
        :param request_id: 用人申请ID
        :return: 前端显示的数据字典
        """
        try:
            print(f"正在获取用人申请详情，ID: {request_id}")
            print(f"当前URL: {self.driver.current_url}")
            
            # 如果不在dashboard页面，先导航到dashboard
            if "/dashboard" not in self.driver.current_url:
                print(f"不在dashboard页面，正在导航到dashboard...")
                self.open("/dashboard")
            
            print(f"当前URL: {self.driver.current_url}")
            print(f"正在点击用人申请菜单...")
            # 点击用人申请菜单
            self.click((By.XPATH, "//span[contains(text(),'用人申请')]"))
            
            # 等待表格加载
            try:
                self.wait.until(
                    EC.presence_of_element_located((By.TAG_NAME, "table"))
                )
            except:
                pass
            
            print(f"正在查找查看按钮...")
            # 尝试多种方式找到查看按钮
            view_buttons = []
            
            # 方式1: 通过文本查找
            try:
                view_buttons = self.driver.find_elements(By.XPATH, "//span[text()='查看']/ancestor::button")
                print(f"方式1找到 {len(view_buttons)} 个查看按钮")
            except:
                pass
            
            # 方式2: 通过title属性查找
            if not view_buttons:
                try:
                    view_buttons = self.driver.find_elements(By.XPATH, "//button[@title='查看']")
                    print(f"方式2找到 {len(view_buttons)} 个查看按钮")
                except:
                    pass
            
            # 方式3: 通过class和文本查找
            if not view_buttons:
                try:
                    view_buttons = self.driver.find_elements(By.XPATH, "//button[contains(@class, 'ant-btn') and .//span[text()='查看']]")
                    print(f"方式3找到 {len(view_buttons)} 个查看按钮")
                except:
                    pass
            
            # 方式4: 打印所有按钮用于调试
            if not view_buttons:
                try:
                    all_buttons = self.driver.find_elements(By.TAG_NAME, "button")
                    print(f"页面共有 {len(all_buttons)} 个按钮")
                    for i, btn in enumerate(all_buttons[:5]):
                        print(f"  按钮 {i+1}: {btn.get_attribute('outerHTML')[:200]}")
                except:
                    pass
            
            # 如果没有指定request_id，直接点击第一个查看按钮
            if request_id is None:
                if view_buttons:
                    print(f"点击第一个查看按钮")
                    view_buttons[0].click()
                    # 等待详情页面加载
                    try:
                        self.wait.until(
                            EC.presence_of_element_located((By.XPATH, "//span[contains(text(),'岗位标题')]"))
                        )
                    except:
                        pass
                else:
                    print(f"未找到查看按钮")
                    return None
            else:
                # 找到对应的记录ID
                # 获取所有记录的ID
                record_ids = []
                try:
                    # 获取表格的所有行
                    rows = self.driver.find_elements(By.XPATH, "//tbody/tr")
                    print(f"找到 {len(rows)} 行数据")
                    
                    # 遍历每一行，获取第一列（ID列）
                    for i, row in enumerate(rows):
                        try:
                            cells = row.find_elements(By.TAG_NAME, "td")
                            print(f"第 {i+1} 行有 {len(cells)} 个单元格")
                            for j, cell in enumerate(cells):
                                cell_text = cell.text.strip()
                                print(f"  单元格 {j+1}: {cell_text}")
                                if j == 0 and cell_text.isdigit():
                                    record_ids.append(int(cell_text))
                        except Exception as e:
                            print(f"处理第 {i+1} 行时出错: {e}")
                    
                    print(f"找到的记录ID: {record_ids}")
                    
                    # 找到对应ID的查看按钮索引
                    if request_id in record_ids:
                        button_index = record_ids.index(request_id)
                        if button_index < len(view_buttons):
                            print(f"点击第 {button_index + 1} 个查看按钮（ID: {request_id}）")
                            view_buttons[button_index].click()
                            # 等待详情页面加载
                            try:
                                self.wait.until(
                                    EC.presence_of_element_located((By.XPATH, "//span[contains(text(),'岗位标题')]"))
                                )
                            except:
                                pass
                        else:
                            print(f"找不到ID为 {request_id} 的查看按钮")
                            return None
                    else:
                        print(f"ID {request_id} 不在当前页面")
                        # 点击第一个查看按钮
                        view_buttons[0].click()
                        # 等待详情页面加载
                        try:
                            self.wait.until(
                                EC.presence_of_element_located((By.XPATH, "//span[contains(text(),'岗位标题')]"))
                            )
                        except:
                            pass
                except Exception as e:
                    print(f"获取记录ID失败: {e}")
                    import traceback
                    traceback.print_exc()
                    # 点击第一个查看按钮
                    if view_buttons:
                        view_buttons[0].click()
                        # 等待详情页面加载
                        try:
                            self.wait.until(
                                EC.presence_of_element_located((By.XPATH, "//span[contains(text(),'岗位标题')]"))
                            )
                        except:
                            pass
                    else:
                        return None
            
            # 如果没找到查看按钮，打印页面信息
            if len(view_buttons) == 0:
                print(f"页面HTML: {self.driver.page_source[:1000]}")
                print(f"所有按钮: {[btn.get_attribute('outerHTML') for btn in self.driver.find_elements(By.TAG_NAME, 'button')]}")
                print(f"所有带title属性的元素: {[el.get_attribute('title') for el in self.driver.find_elements(By.XPATH, '//*[@title]')]}")
                return None
            
            print(f"正在获取详情页面数据...")
            # 获取详情页面的数据
            detail_data = {}
            
            # 获取岗位标题
            try:
                title_element = self.driver.find_element(By.XPATH, "//span[contains(text(),'岗位标题')]/following-sibling::span")
                detail_data['岗位标题'] = title_element.text
                print(f"岗位标题: {title_element.text}")
            except Exception as e:
                detail_data['岗位标题'] = None
                print(f"获取岗位标题失败: {e}")
            
            # 获取所属团队
            try:
                team_element = self.driver.find_element(By.XPATH, "//span[contains(text(),'所属团队')]/following-sibling::span")
                detail_data['所属团队'] = team_element.text
                print(f"所属团队: {team_element.text}")
            except Exception as e:
                detail_data['所属团队'] = None
                print(f"获取所属团队失败: {e}")
            
            # 获取技术平台
            try:
                platform_element = self.driver.find_element(By.XPATH, "//span[contains(text(),'技术平台')]/following-sibling::span")
                detail_data['技术平台'] = platform_element.text
                print(f"技术平台: {platform_element.text}")
            except Exception as e:
                detail_data['技术平台'] = None
                print(f"获取技术平台失败: {e}")
            
            # 获取补充人数
            try:
                count_element = self.driver.find_element(By.XPATH, "//span[contains(text(),'补充人数')]/following-sibling::span")
                detail_data['补充人数'] = count_element.text
                print(f"补充人数: {count_element.text}")
            except Exception as e:
                detail_data['补充人数'] = None
                print(f"获取补充人数失败: {e}")
            
            # 获取建议级别
            try:
                level_element = self.driver.find_element(By.XPATH, "//span[contains(text(),'建议级别')]/following-sibling::span")
                detail_data['建议级别'] = level_element.text
                print(f"建议级别: {level_element.text}")
            except Exception as e:
                detail_data['建议级别'] = None
                print(f"获取建议级别失败: {e}")
            
            # 获取审批状态
            try:
                status_element = self.driver.find_element(By.XPATH, "//span[contains(text(),'审批状态')]/following-sibling::span")
                detail_data['审批状态'] = status_element.text
                print(f"审批状态: {status_element.text}")
            except Exception as e:
                detail_data['审批状态'] = None
                print(f"获取审批状态失败: {e}")
            
            print(f"详情数据: {detail_data}")
            return detail_data
        except Exception as e:
            print(f"获取用人申请详情失败: {e}")
            import traceback
            traceback.print_exc()
            return None
    
    def get_param_display(self, param_type, param_code):
        """
        获取参数在前端的显示值
        :param param_type: 参数类型
        :param param_code: 参数代码
        :return: 前端显示的参数名称
        """
        try:
            # 确保在dashboard页面
            if "/dashboard" not in self.driver.current_url:
                print(f"当前不在dashboard页面，正在导航到dashboard...")
                self.open("/dashboard")
                # 等待页面加载
                try:
                    self.wait.until(EC.url_contains("/dashboard"))
                except:
                    pass
            
            # 等待页面加载
            try:
                self.short_wait.until(
                    EC.presence_of_element_located((By.TAG_NAME, "body"))
                )
            except:
                pass
            
            # 点击用人申请菜单
            try:
                self.click((By.XPATH, "//span[contains(text(),'用人申请')]"))
            except Exception as e:
                print(f"点击用人申请菜单失败: {e}")
                print(f"当前URL: {self.driver.current_url}")
                print(f"页面标题: {self.driver.title}")
                # 打印页面内容用于调试
                print(f"页面HTML片段: {self.driver.page_source[:500]}")
                return None
            
            # 点击新增按钮
            try:
                print(f"正在查找新增按钮...")
                self.click((By.CSS_SELECTOR, "[title='新增用人申请']"))
                print(f"成功点击新增按钮")
            except Exception as e:
                print(f"点击新增按钮失败: {e}")
                # 尝试其他方式找到新增按钮
                try:
                    print(f"尝试使用其他方式查找新增按钮...")
                    new_button = self.short_wait.until(
                        EC.element_to_be_clickable((By.XPATH, "//span[contains(text(),'新增')]/ancestor::button"))
                    )
                    new_button.click()
                    print(f"成功使用其他方式点击新增按钮")
                except Exception as e2:
                    print(f"其他方式也失败: {e2}")
                    return None
            
            # 等待页面跳转或弹窗打开
            try:
                print(f"等待页面加载...")
                # 等待页面跳转到new页面或弹窗打开
                self.wait.until(
                    lambda driver: "/recruitment-request/new" in driver.current_url or 
                    len(driver.find_elements(By.CLASS_NAME, "ant-modal")) > 0
                )
                print(f"页面已加载")
            except Exception as e:
                print(f"等待页面加载超时: {e}")
                # 打印页面内容用于调试
                print(f"当前URL: {self.driver.current_url}")
                print(f"页面标题: {self.driver.title}")
                return None
            
            # 根据参数类型找到对应的下拉框
            if param_type == 'LEVEL':
                label = '建议级别'
            elif param_type == 'TEAM':
                label = '所属团队'
            elif param_type == 'PLATFORM':
                label = '技术平台'
            else:
                return None
            
            # 获取下拉框的选项
            try:
                # 尝试多种方式找到下拉框
                select_element = None
                
                # 方式1: 通过span文本和following select（原生select）
                try:
                    select_element = self.short_wait.until(
                        EC.presence_of_element_located((By.XPATH, f"//span[contains(text(),'{label}')]/following::select[1]"))
                    )
                    print(f"方式1找到下拉框（原生select）")
                except:
                    pass
                
                # 方式2: 通过label文本和following select（原生select）
                if not select_element:
                    try:
                        select_element = self.short_wait.until(
                            EC.presence_of_element_located((By.XPATH, f"//label[contains(text(),'{label}')]/following::select[1]"))
                        )
                        print(f"方式2找到下拉框（原生select）")
                    except:
                        pass
                
                # 方式3: 通过span文本和父级div（原生select）
                if not select_element:
                    try:
                        select_element = self.short_wait.until(
                            EC.presence_of_element_located((By.XPATH, f"//span[contains(text(),'{label}')]/parent::div/following-sibling::div//select"))
                        )
                        print(f"方式3找到下拉框（原生select）")
                    except:
                        pass
                
                # 方式4: Ant Design Select组件（点击后展开）
                if not select_element:
                    try:
                        # 找到包含label的div
                        label_div = self.short_wait.until(
                            EC.presence_of_element_located((By.XPATH, f"//span[contains(text(),'{label}')]/parent::div"))
                        )
                        print(f"找到label的父div")
                        
                        # 点击下拉框展开选项
                        label_div.click()
                        print(f"点击下拉框展开选项")
                        
                        # 等待选项列表出现
                        option_list = self.short_wait.until(
                            EC.presence_of_element_located((By.CLASS_NAME, "ant-select-dropdown"))
                        )
                        print(f"选项列表已展开")
                        
                        # 获取所有选项
                        options = option_list.find_elements(By.CLASS_NAME, "ant-select-item-option")
                        print(f"找到 {len(options)} 个选项")
                        
                        # 遍历选项，找到匹配的参数代码
                        for option in options:
                            option_text = option.text.strip()
                            option_value = option.get_attribute('title')
                            print(f"参数选项: value={option_value}, text={option_text}")
                            
                            # 检查是否包含参数代码
                            if param_code in option_text or option_value == param_code:
                                return option_text
                        
                        print(f"未找到参数代码为 {param_code} 的选项")
                        return None
                    except Exception as e:
                        print(f"Ant Design Select组件查找失败: {e}")
                        pass
                
                # 方式5: 直接查找所有select元素（原生select）
                if not select_element:
                    try:
                        all_selects = self.driver.find_elements(By.TAG_NAME, "select")
                        print(f"页面共有 {len(all_selects)} 个select元素")
                        for i, sel in enumerate(all_selects):
                            print(f"  Select {i+1}: {sel.get_attribute('outerHTML')[:200]}")
                    except:
                        pass
                
                # 方式6: 查找所有包含label的div
                if not select_element:
                    try:
                        all_divs = self.driver.find_elements(By.XPATH, f"//div[contains(.,'{label}')]")
                        print(f"找到 {len(all_divs)} 个包含'{label}'的div")
                        for i, div in enumerate(all_divs[:3]):
                            print(f"  Div {i+1}: {div.get_attribute('outerHTML')[:300]}")
                    except:
                        pass
                
                if not select_element:
                    print(f"未找到{label}下拉框")
                    return None
                
                from selenium.webdriver.support.ui import Select
                select = Select(select_element)
                
                # 遍历选项，找到匹配的参数代码
                for option in select.options:
                    option_value = option.get_attribute('value')
                    option_text = option.text
                    print(f"参数选项: value={option_value}, text={option_text}")
                    if option_value == param_code:
                        return option_text
                
                print(f"未找到参数代码为 {param_code} 的选项")
                return None
            except Exception as e:
                print(f"获取下拉框选项失败: {e}")
                import traceback
                traceback.print_exc()
                return None
        except Exception as e:
            print(f"获取参数显示失败: {e}")
            import traceback
            traceback.print_exc()
            return None
    
    def get_approval_status_display(self, request_id):
        """
        获取审批状态在前端的显示值
        :param request_id: 用人申请ID
        :return: 前端显示的审批状态
        """
        try:
            # 打开用人申请页面
            self.open("/")
            
            # 点击用人申请菜单
            self.click((By.XPATH, "//span[contains(text(),'用人申请')]"))
            
            # 等待表格加载
            try:
                self.wait.until(
                    EC.presence_of_element_located((By.CSS_SELECTOR, "table"))
                )
            except:
                pass
            
            # 找到对应的记录的审批状态列
            try:
                status_elements = self.driver.find_elements(By.CSS_SELECTOR, "td[data-column-key='approvalStatus']")
                if status_elements:
                    return status_elements[0].text
            except:
                pass
            
            return None
        except Exception as e:
            print(f"获取审批状态显示失败: {e}")
            return None
    
    def get_team_display(self, request_id):
        """
        获取所属团队在前端的显示值
        :param request_id: 用人申请ID
        :return: 前端显示的所属团队
        """
        try:
            # 打开用人申请页面
            self.open("/")
            
            # 点击用人申请菜单
            self.click((By.XPATH, "//span[contains(text(),'用人申请')]"))
            
            # 等待表格加载
            try:
                self.wait.until(
                    EC.presence_of_element_located((By.CSS_SELECTOR, "table"))
                )
            except:
                pass
            
            # 找到对应的记录的所属团队列
            try:
                team_elements = self.driver.find_elements(By.CSS_SELECTOR, "td[data-column-key='team']")
                if team_elements:
                    return team_elements[0].text
            except:
                pass
            
            return None
        except Exception as e:
            print(f"获取所属团队显示失败: {e}")
            return None
    
    def get_technical_platform_display(self, request_id):
        """
        获取技术平台在前端的显示值
        :param request_id: 用人申请ID
        :return: 前端显示的技术平台
        """
        try:
            # 打开用人申请页面
            self.open("/")
            
            # 点击用人申请菜单
            self.click((By.XPATH, "//span[contains(text(),'用人申请')]"))
            
            # 等待表格加载
            try:
                self.wait.until(
                    EC.presence_of_element_located((By.CSS_SELECTOR, "table"))
                )
            except:
                pass
            
            # 找到对应的记录的技术平台列
            try:
                platform_elements = self.driver.find_elements(By.CSS_SELECTOR, "td[data-column-key='technicalPlatform']")
                if platform_elements:
                    return platform_elements[0].text
            except:
                pass
            
            return None
        except Exception as e:
            print(f"获取技术平台显示失败: {e}")
            return None

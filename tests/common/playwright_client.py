# Playwright UI客户端模块，用于处理前端UI测试

from playwright.sync_api import sync_playwright, Page, expect
from .config import config

class PlaywrightClient:
    def __init__(self, headless=True, browser='chromium'):
        """
        初始化Playwright客户端
        :param headless: 是否以无头模式运行
        :param browser: 浏览器类型 ('chromium', 'firefox', 'webkit')
        """
        self.browser = browser
        self.headless = headless
        self.playwright = None
        self.browser_instance = None
        self.context = None
        self.page = None
        self.base_url = config.FRONTEND_URL
        
        self._init_browser()
    
    def _init_browser(self):
        """初始化浏览器"""
        self.playwright = sync_playwright().start()
        self.browser_instance = self.playwright[self.browser].launch(headless=self.headless)
        self.context = self.browser_instance.new_context()
        self.page = self.context.new_page()
        self.page.set_default_timeout(config.UI_TIMEOUT * 1000)
    
    def open(self, path=""):
        """
        打开指定路径的页面
        :param path: 页面路径
        """
        url = f"{self.base_url}{path}"
        self.page.goto(url)
        self.page.wait_for_load_state("networkidle")
    
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
            print(f"登录页面已打开，当前URL: {self.page.url}")
            
            # 输入用户名
            print(f"正在输入用户名: {username}")
            self.page.fill("input[placeholder='用户名']", username)
            
            # 输入密码
            print(f"正在输入密码...")
            self.page.fill("input[placeholder='密码']", password)
            
            # 点击登录按钮
            print(f"正在点击登录按钮...")
            self.page.click("button:has-text('登录')")
            
            # 等待登录成功，跳转到首页
            print(f"等待跳转到dashboard...")
            self.page.wait_for_url(f"{self.base_url}/dashboard", timeout=config.UI_TIMEOUT * 1000)
            print(f"登录成功，当前URL: {self.page.url}")
            return True
        except Exception as e:
            print(f"登录失败: {e}")
            print(f"当前URL: {self.page.url}")
            import traceback
            traceback.print_exc()
            return False
    
    def click(self, selector):
        """
        点击元素
        :param selector: 元素选择器
        """
        self.page.click(selector)
        self.page.wait_for_load_state("networkidle")
    
    def fill(self, selector, text):
        """
        填充文本
        :param selector: 元素选择器
        :param text: 输入的文本
        """
        self.page.fill(selector, text)
    
    def select(self, selector, value):
        """
        选择下拉框选项
        :param selector: 元素选择器
        :param value: 选项值
        """
        self.page.select_option(selector, value)
    
    def get_text(self, selector):
        """
        获取元素文本
        :param selector: 元素选择器
        :return: 元素文本
        """
        return self.page.inner_text(selector)
    
    def is_element_present(self, selector):
        """
        检查元素是否存在
        :param selector: 元素选择器
        :return: 是否存在
        """
        return self.page.locator(selector).count() > 0
    
    def get_recruitment_request_detail(self, request_id):
        """
        获取用人申请详情的前端显示数据
        :param request_id: 用人申请ID
        :return: 前端显示的数据字典
        """
        try:
            print(f"正在获取用人申请详情，ID: {request_id}")
            print(f"当前URL: {self.page.url}")
            
            # 确保在dashboard页面
            if "/dashboard" not in self.page.url:
                print(f"不在dashboard页面，正在导航到dashboard...")
                self.open("/dashboard")
            
            print(f"当前URL: {self.page.url}")
            print(f"正在点击用人申请菜单...")
            # 点击用人申请菜单
            self.click("span:has-text('用人申请')")
            
            print(f"正在查找查看按钮...")
            # 找到对应的记录并点击查看
            view_buttons = self.page.locator("button:has(span:text('查看'))")
            button_count = view_buttons.count()
            print(f"找到 {button_count} 个查看按钮")
            
            # 如果没有指定request_id，直接点击第一个查看按钮
            if request_id is None:
                if button_count > 0:
                    print(f"点击第一个查看按钮")
                    view_buttons.first.click()
                    # 等待详情页面加载
                    self.page.wait_for_selector("span:has-text('岗位标题')", timeout=config.UI_TIMEOUT * 1000)
                else:
                    print(f"未找到查看按钮")
                    return None
            else:
                # 找到对应的记录ID
                # 获取所有记录的ID
                record_ids = []
                try:
                    # 获取表格的所有行
                    rows = self.page.locator("tbody tr")
                    row_count = rows.count()
                    print(f"找到 {row_count} 行数据")
                    
                    # 遍历每一行，获取第一列（ID列）
                    for i in range(row_count):
                        try:
                            cells = rows.nth(i).locator("td")
                            cell_count = cells.count()
                            for j in range(cell_count):
                                cell_text = cells.nth(j).inner_text().strip()
                                print(f"  单元格 {j+1}: {cell_text}")
                                if j == 0 and cell_text.isdigit():
                                    record_ids.append(int(cell_text))
                        except Exception as e:
                            print(f"处理第 {i+1} 行时出错: {e}")
                    
                    print(f"找到的记录ID: {record_ids}")
                    
                    # 找到对应ID的查看按钮索引
                    if request_id in record_ids:
                        button_index = record_ids.index(request_id)
                        if button_index < button_count:
                            print(f"点击第 {button_index + 1} 个查看按钮（ID: {request_id}）")
                            view_buttons.nth(button_index).click()
                            # 等待详情页面加载
                            self.page.wait_for_selector("span:has-text('岗位标题')", timeout=config.UI_TIMEOUT * 1000)
                        else:
                            print(f"找不到ID为 {request_id} 的查看按钮")
                            return None
                    else:
                        print(f"ID {request_id} 不在当前页面")
                        # 点击第一个查看按钮
                        view_buttons.first.click()
                        # 等待详情页面加载
                        self.page.wait_for_selector("span:has-text('岗位标题')", timeout=config.UI_TIMEOUT * 1000)
                except Exception as e:
                    print(f"获取记录ID失败: {e}")
                    import traceback
                    traceback.print_exc()
                    # 点击第一个查看按钮
                    if button_count > 0:
                        view_buttons.first.click()
                        # 等待详情页面加载
                        self.page.wait_for_selector("span:has-text('岗位标题')", timeout=config.UI_TIMEOUT * 1000)
                    else:
                        return None
            
            # 如果没找到查看按钮，打印页面信息
            if button_count == 0:
                print(f"未找到查看按钮")
                return None
            
            print(f"正在获取详情页面数据...")
            # 获取详情页面的数据
            detail_data = {}
            
            # 获取岗位标题
            try:
                detail_data['岗位标题'] = self.get_text("span:has-text('岗位标题') + span")
                print(f"岗位标题: {detail_data['岗位标题']}")
            except Exception as e:
                detail_data['岗位标题'] = None
                print(f"获取岗位标题失败: {e}")
            
            # 获取所属团队
            try:
                detail_data['所属团队'] = self.get_text("span:has-text('所属团队') + span")
                print(f"所属团队: {detail_data['所属团队']}")
            except Exception as e:
                detail_data['所属团队'] = None
                print(f"获取所属团队失败: {e}")
            
            # 获取技术平台
            try:
                detail_data['技术平台'] = self.get_text("span:has-text('技术平台') + span")
                print(f"技术平台: {detail_data['技术平台']}")
            except Exception as e:
                detail_data['技术平台'] = None
                print(f"获取技术平台失败: {e}")
            
            # 获取补充人数
            try:
                detail_data['补充人数'] = self.get_text("span:has-text('补充人数') + span")
                print(f"补充人数: {detail_data['补充人数']}")
            except Exception as e:
                detail_data['补充人数'] = None
                print(f"获取补充人数失败: {e}")
            
            # 获取建议级别
            try:
                detail_data['建议级别'] = self.get_text("span:has-text('建议级别') + span")
                print(f"建议级别: {detail_data['建议级别']}")
            except Exception as e:
                detail_data['建议级别'] = None
                print(f"获取建议级别失败: {e}")
            
            # 获取审批状态
            try:
                status_element = self.page.locator("span:has-text('审批状态') + span")
                if status_element.count() > 0:
                    detail_data['审批状态'] = status_element.inner_text()
                    print(f"审批状态: {detail_data['审批状态']}")
                else:
                    # 尝试获取Tag组件的文本
                    status_tag = self.page.locator("span:has-text('审批状态')").locator("..").locator(".ant-tag")
                    if status_tag.count() > 0:
                        detail_data['审批状态'] = status_tag.inner_text()
                        print(f"审批状态: {detail_data['审批状态']}")
                    else:
                        detail_data['审批状态'] = None
                        print(f"获取审批状态失败: 未找到元素")
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
    
    def close(self):
        """
        关闭浏览器
        """
        if self.context:
            self.context.close()
        if self.browser_instance:
            self.browser_instance.close()
        if self.playwright:
            self.playwright.stop()
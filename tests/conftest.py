import pytest
from common.ui_client import UiClient
from common.api_client import ApiClient
from common.playwright_client import PlaywrightClient
from common.config import config

@pytest.fixture(scope="session")
def api_client():
    """
    会话级别的API客户端fixture
    在整个测试会话中共享同一个API客户端
    """
    client = ApiClient()
    client.login(config.TEST_USERS["admin"]["username"], config.TEST_USERS["admin"]["password"])
    yield client
    client.logout()

@pytest.fixture(scope="function")
def ui_client():
    """
    函数级别的UI客户端fixture（Selenium）
    每个测试函数都会创建新的浏览器实例
    注意：并行测试时，每个worker进程会有独立的浏览器实例
    """
    try:
        client = UiClient(headless=True, browser='auto')
        client.login(config.TEST_USERS["admin"]["username"], config.TEST_USERS["admin"]["password"])
        yield client
    except RuntimeError as e:
        error_msg = str(e)
        if '允许远程自动化' in error_msg or '连接超时' in error_msg or 'timed out' in error_msg or 'already paired' in error_msg:
            pytest.skip(f"Safari浏览器不可用: {error_msg}")
        raise
    finally:
        try:
            client.close()
        except:
            pass

@pytest.fixture(scope="function")
def playwright_client():
    """
    函数级别的Playwright客户端fixture
    每个测试函数都会创建新的浏览器实例
    """
    client = PlaywrightClient(headless=True, browser='chromium')
    yield client
    client.close()
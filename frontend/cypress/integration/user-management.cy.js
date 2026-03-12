/// <reference types="cypress" />

const loginResponse = {
  returnCode: 'SUC0000',
  errorMsg: '',
  body: {
    token: 'header.payload.signature',
    user: {
      userId: '1001',
      username: 'admin',
      realName: '系统管理员',
      department: '技术管理团队',
      position: '系统管理员',
      status: 'ACTIVE',
    },
    permissions: [],
  },
}

const departmentOptionsResponse = {
  returnCode: 'SUC0000',
  errorMsg: '',
  body: {
    companyName: '永隆信息有限公司',
    teamOptions: [
      {
        teamName: '零售业务开发团队',
        teamType: 'NORMAL',
        requiresGroup: true,
        groupOptions: [{ groupName: '零售平台开发室' }],
      },
      {
        teamName: '技术管理团队',
        teamType: 'DIRECT',
        requiresGroup: false,
        groupOptions: [],
      },
      {
        teamName: '直属人员',
        teamType: 'DIRECT_CATEGORY',
        requiresGroup: true,
        groupOptions: [{ groupName: '部门总经理' }, { groupName: '分管总' }],
      },
    ],
  },
}

const usersResponse = {
  returnCode: 'SUC0000',
  errorMsg: '',
  body: [
    {
      userId: '1002',
      username: 'zhangsan',
      realName: '张三',
      email: 'a@b.com',
      phone: '13800000000',
      position: '开发工程师',
      status: 'ACTIVE',
      department: '零售平台开发室',
      teamName: '零售业务开发团队',
      groupName: '零售平台开发室',
      departmentType: 'TEAM_GROUP',
      departmentDisplay: '零售业务开发团队 / 零售平台开发室',
    },
  ],
}

const positionParamsResponse = {
  returnCode: 'SUC0000',
  errorMsg: '',
  body: [{ paramCode: 'DEV_ENGINEER', paramValue: '开发工程师', paramName: '开发工程师' }],
}

const emptyParamsResponse = {
  returnCode: 'SUC0000',
  errorMsg: '',
  body: [],
}

describe('用户管理部门契约', () => {
  beforeEach(() => {
    cy.intercept('POST', 'http://localhost:8080/api/auth/login', loginResponse).as('login')
    cy.intercept('GET', 'http://localhost:8080/api/message/user/1001', { returnCode: 'SUC0000', errorMsg: '', body: [] })
    cy.intercept('GET', 'http://localhost:8080/api/message/unread-count/1001', { returnCode: 'SUC0000', errorMsg: '', body: { unreadCount: 0 } })
    cy.intercept('GET', 'http://localhost:8080/api/interview/pending/count', { returnCode: 'SUC0000', errorMsg: '', body: 0 })
    cy.intercept('GET', 'http://localhost:8080/api/sys/params/active/type/POSITION', positionParamsResponse).as('getPositions')
    cy.intercept('GET', 'http://localhost:8080/api/sys/params/active/type/LEVEL', emptyParamsResponse)
    cy.intercept('GET', 'http://localhost:8080/api/sys/params/active/type/team', emptyParamsResponse)
    cy.intercept('GET', 'http://localhost:8080/api/sys/params/active/type/TEAM', emptyParamsResponse)
    cy.intercept('GET', 'http://localhost:8080/api/sys/params/active/type/PLATFORM', emptyParamsResponse)
    cy.intercept('GET', 'http://localhost:8080/api/sys/params/active/type/CATEGORY', emptyParamsResponse)
    cy.intercept('GET', 'http://localhost:8080/api/users', usersResponse).as('getUsers')
    cy.intercept('GET', 'http://localhost:8080/api/users/department-options', departmentOptionsResponse).as('getDepartmentOptions')
    cy.intercept('POST', 'http://localhost:8080/api/users', {
      returnCode: 'SUC0000',
      errorMsg: '',
      body: { userId: '1003' },
    }).as('createUser')

    cy.visit('/login')
    cy.get('input[name="username"]').type('admin')
    cy.get('input[name="password"]').type('123321')
    cy.get('button[type="submit"]').click()
    cy.wait('@login')
    cy.url().should('include', '/dashboard')
  })

  it('在列表中展示部门字段', () => {
    cy.visit('/user-management')
    cy.wait('@getUsers')

    cy.contains('th', '部门').should('be.visible')
    cy.contains('零售业务开发团队 / 零售平台开发室').should('be.visible')
  })

  it('新增用户时支持团队和室组联动', () => {
    cy.visit('/user-management')
    cy.wait('@getUsers')
    cy.wait('@getPositions')

    cy.contains('button', '新增用户').click()
    cy.wait('@getDepartmentOptions')

    cy.contains('label', '团队名称').should('be.visible')
    cy.contains('label', '室组名称').should('be.visible')

    cy.get('input[placeholder="请输入用户ID"]').type('1003')
    cy.get('input[placeholder="请输入用户名"]').type('lisi')
    cy.get('input[placeholder="请输入密码"]').type('123456')
    cy.get('input[placeholder="请输入真实姓名"]').type('李四')

    cy.contains('.ant-form-item', '团队名称').find('.ant-select').click()
    cy.get('.ant-select-dropdown').contains('零售业务开发团队').click()
    cy.contains('.ant-form-item', '室组名称').find('.ant-select').click()
    cy.get('.ant-select-dropdown').contains('零售平台开发室').click()
    cy.contains('.ant-form-item', '岗位').find('.ant-select').click()
    cy.get('.ant-select-dropdown').contains('开发工程师').click()
    cy.contains('.ant-form-item', '状态').find('.ant-select').click()
    cy.get('.ant-select-dropdown').contains('启用').click()

    cy.contains('.ant-modal-footer button', '确定').click()
    cy.wait('@createUser')
      .its('request.body')
      .should('deep.include', {
        teamName: '零售业务开发团队',
        groupName: '零售平台开发室',
      })
  })
})

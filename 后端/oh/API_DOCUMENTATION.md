 后端服务API文档

## 1. 用户管理相关接口

### 1.1 用户注册
- **URL**: `/api/user/register`
- **Method**: POST
- **Description**: 用户注册
- **Request Body**: 
  ```json
  {
    "name": "username",
    "password": "password123"
  }
  ```
- **Response**: 
  ```json
  {
    "message": "注册成功"
  }
  ```
- **Error Response**: 
  ```json
  {
    "message": "用户名已存在"
  }
  ```

### 1.2 用户登录
- **URL**: `/api/user/login`
- **Method**: POST
- **Description**: 用户登录
- **Request Body**: 
  ```json
  {
    "name": "username",
    "password": "password123"
  }
  ```
- **Response**: 
  ```json
  {
    "message": "登录成功"
  }
  ```
- **Error Response**: 
  ```json
  {
    "message": "用户名或密码错误"
  }
  ```

### 1.3 获取所有用户
- **URL**: `/api/user/all`
- **Method**: GET
- **Description**: 获取所有用户信息
- **Response**: 
  ```json
  [
    {
      "id": 1,
      "count": "user1",
      "password": "password123"
    },
    {
      "id": 2,
      "count": "user2",
      "password": "password456"
    }
  ]
  ```

### 1.4 修改密码
- **URL**: `/api/user/password`
- **Method**: PUT
- **Description**: 修改用户密码
- **Query Parameters**:
  - `count`: 用户名
  - `newPassword`: 新密码
- **Response**: 
  ```json
  {
    "message": "密码修改成功"
  }
  ```

## 2. 温湿度记录相关接口

### 2.1 新增温湿度记录
- **URL**: `/api/ohos/record`
- **Method**: POST
- **Description**: 新增一条温湿度记录
- **Request Body**: 
  ```json
  {
    "ohosTemperature": 25.5,
    "ohosHumidity": 60.0
  }
  ```
- **Response**: 
  ```json
  {
    "message": "Record saved"
  }
  ```

### 2.2 获取所有记录
- **URL**: `/api/ohos/all`
- **Method**: GET
- **Description**: 获取最近10条温湿度记录
- **Response**: 
  ```json
  [
    {
      "id": 1,
      "ohosTemperature": 25.5,
      "ohosHumidity": 60.0,
      "createTime": "2024-03-20T10:30:00"
    },
    {
      "id": 2,
      "ohosTemperature": 26.0,
      "ohosHumidity": 58.0,
      "createTime": "2024-03-20T10:35:00"
    }
  ]
  ```

### 2.3 获取所有温度数据
- **URL**: `/api/ohos/temperatures`
- **Method**: GET
- **Description**: 获取最近10条温度数据
- **Response**: 
  ```json
  [25.5, 26.0, 25.8, 26.2]
  ```

### 2.4 获取所有湿度数据
- **URL**: `/api/ohos/humidities`
- **Method**: GET
- **Description**: 获取最近10条湿度数据
- **Response**: 
  ```json
  [60.0, 58.0, 59.5, 57.8]
  ```

## 3. DeepSeek AI相关接口

### 3.1 询问DeepSeek
- **URL**: `/api/askDeepSeek`
- **Method**: GET
- **Description**: 向DeepSeek AI模型发送问题
- **Query Parameters**:
  - `question`: 要询问的问题
- **Request Example**:
  ```
  GET /api/askDeepSeek?question=什么是人工智能？
  ```
- **Response**: 
  ```json
  {
    "answer": "人工智能是计算机科学的一个分支，它致力于创造能够模拟人类智能的机器..."
  }
  ```
- **Error Response**:
  ```json
  {
    "message": "Error askDeepSeek question"
  }
  ```

## 4. MQTT相关接口

### 4.1 发送MQTT消息
- **URL**: `/send`
- **Method**: POST
- **Description**: 发送消息到MQTT服务器
- **Request Body**: 
  ```json
  {
    "signal": 4,
    "value": 25.5
  }
  ```
- **Response**: 
  ```json
  {
    "message": "Message sent: {\"signal\":4,\"value\":25.5}"
  }
  ```

## 5. WebSocket接口

### 5.1 WebSocket连接
- **URL**: `/ws`
- **Method**: WebSocket
- **Description**: 建立WebSocket连接，用于实时数据传输
- **连接建立**:
  - 客户端发送连接请求
  - 服务器返回: `"连接成功"`
- **消息格式**:
  - 客户端发送:
    ```json
    {
      "signal": 4,
      "value": 25.5
    }
    ```
  - 服务器推送:
    ```json
    {
      "signal": 9,
      "value": 60.0
    }
    ```

## 信号常量定义

系统定义了以下信号常量用于MQTT通信：

| 信号ID | 描述 | 请求/响应示例 |
|--------|------|--------------|
| 0 | 人离开 | `{"signal":0,"value":0}` |
| 1 | 人到来 | `{"signal":1,"value":0}` |
| 2 | 开灯 | `{"signal":2,"value":0}` |
| 3 | 关灯 | `{"signal":3,"value":0}` |
| 4 | 获取温度 | `{"signal":4,"value":0}` |
| 6 | 开门 | `{"signal":6,"value":0}` |
| 7 | 关门 | `{"signal":7,"value":0}` |
| 8 | 获取湿度 | `{"signal":8,"value":0}` |
| 9 | 发送湿度 | `{"signal":9,"value":60.0}` |
| 10 | 开风扇 | `{"signal":10,"value":0}` |
| 11 | 关风扇 | `{"signal":11,"value":0}` |
| 12 | 获取光照 | `{"signal":12,"value":0}` |
| 13 | 发送光照 | `{"signal":13,"value":500}` |
| 111 | 其他信号 | `{"signal":111,"value":0}` | 
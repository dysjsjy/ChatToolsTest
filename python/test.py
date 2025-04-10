import requests
import json

class ChatBot:
    def __init__(self, api_url="http://localhost:11434"):
        self.api_url = api_url
        # 初始化对话历史
        self.conversation_history = []

    def send_message(self, message, model="qwen-2.5-1.5B-Instruct"):  # 默认使用llama2模型，你可以根据需要修改
        # 将用户消息添加到对话历史
        self.conversation_history.append({"role": "user", "content": message})
        
        # 构造请求数据
        payload = {
            "model": model,
            "messages": self.conversation_history,
            "stream": False  # 设置为False以获取完整响应
        }
        
        try:
            # 发送POST请求到本地ollama API
            response = requests.post(
                f"{self.api_url}/api/chat",
                json=payload,
                headers={"Content-Type": "application/json"}
            )
            
            # 检查响应状态
            response.raise_for_status()
            
            # 解析响应
            result = response.json()
            assistant_message = result["message"]["content"]
            
            # 将助手回复添加到对话历史
            self.conversation_history.append({"role": "assistant", "content": assistant_message})
            
            return assistant_message
            
        except requests.exceptions.RequestException as e:
            return f"错误: 无法连接到API - {str(e)}"

    def get_history(self):
        # 返回完整的对话历史
        return self.conversation_history

def main():
    # 创建聊天机器人实例
    bot = ChatBot()
    
    print("欢迎使用本地聊天机器人！输入 'quit' 退出")
    
    while True:
        # 获取用户输入
        user_input = input("\n你: ")
        
        # 检查是否退出
        if user_input.lower() == 'quit':
            print("再见！")
            break
            
        # 发送消息并获取回复
        response = bot.send_message(user_input)
        print(f"机器人: {response}")

if __name__ == "__main__":
    main()
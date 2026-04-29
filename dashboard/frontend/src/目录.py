import os

def print_tree(start_path, prefix=""):
    """递归打印目录树结构"""

    # 获取当前目录下所有文件与子目录，并排序
    items = sorted(os.listdir(start_path))
    count = len(items)

    for index, name in enumerate(items):
        path = os.path.join(start_path, name)
        connector = "└── " if index == count - 1 else "├── "

        print(prefix + connector + name)

        # 如果是目录，则继续递归
        if os.path.isdir(path):
            extension = "    " if index == count - 1 else "│   "
            print_tree(path, prefix + extension)

# 运行
if __name__ == "__main__":
    root = "."  # 当前目录，可替换为任意路径，例如 "C:/Projects/MyCode"
    print(root)
    print_tree(root)

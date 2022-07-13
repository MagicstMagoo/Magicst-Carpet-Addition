# Magicst-Carpet-Addition

<img alt="icon" src="./src/main/resources/assets/icon.png" width=128 height=128/>

**中文** | [English](./README_EN.md)
### 适用于客户端和服务端 (大概吧 doge
### 这是一个[地毯](https://github.com/gnembon/fabric-carpet)(Carpet mod)的扩展,添加了点~~NotVanilla的~~功能
[![License](https://img.shields.io/github/license/MagicstMagoo/Magicst-Carpet-Addition?style=flat-square)](https://www.gnu.org/licenses/gpl-3.0.en.html)
![MC Versions](https://img.shields.io/badge/For%20MC-1.19.x-red?style=flat-square)
[![Issues](https://img.shields.io/github/issues/MagicstMagoo/Magicst-Carpet-Addition?style=flat-square)](https://github.com/MagicstMagoo/Magicst-Carpet-Addition/issues)
[![CI](https://img.shields.io/github/workflow/status/MagicstMagoo/Magicst-Carpet-Addition/build?label=Build&style=flat-square)](https://github.com/MagicstMagoo/Magicst-carpet-addition/.github/workflows/build.yml)
[![Github Release Downloads](https://img.shields.io/github/downloads/MagicstMagoo/Magicst-Carpet-Addition/total?label=Github%20Release%20Downloads&style=flat-square)](https://github.com/MagicstMagoo/Magicst-Carpet-Additon/releases)

#### 依赖:
- [Fabric Api](https://github.com/FabricMC/fabric) >= 0.39.2
- [Carpet](https://github.com/gnembon/fabric-carpet) >= 1.4.79+v220607

## 规则列表

### 鱼群AI降智 (FixFishAi)

该功能适用于安装了[Carpet](https://github.com/gnembon/fabric-carpet)的客户端与服务端

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `true`, `false`
- 分类: `MCA`

### RSA举报移除 (RmoveChatReport)

该功能移除了1.19以后版本的举报功能 需要搭配 [No Chat Reports](https://github.com/Aizistral-Studios/No-Chat-Reports) 使用
- 类型: `function`
- 默认值: `false`
- 参考选项: `false`, `client`, `server`
- 分类: `MCA`

### PCA 同步协议 (pcaSyncProtocol)

plusls carpet addition sync protocol

PCA 同步协议是一个用于在服务端和客户端之间同步 Entity，BlockEntity 的协议，目前被 [MasaGadget](https://github.com/plusls/MasaGadget) 用于实现多人游戏容器预览。

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `true`, `false`
- 分类: `PCA`, `protocal`

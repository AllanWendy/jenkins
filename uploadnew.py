#!/usr/bin/python
#coding=utf-8 #默认编码格式为utf-8

import time
import urllib2
import time
import json
import mimetypes
import os,sys
import smtplib
from email.mime.text import MIMEText 
import requests

#蒲公英应用上传地址
url = 'http://www.pgyer.com/apiv1/app/upload'
#蒲公英提供的 用户Key
uKey = '3e1ac4d113a38a70a7243c8ccde76a5b'
#上传文件的文件名（这个可随便取，但一定要以 apk 结尾）
file_name = 'app-debug.apk'
#蒲公英提供的 API Key
_api_key = 'da8711b5c5ac60adb3e515f9b30f576f'
#安装应用时需要输入的密码，这个可不填
installPassword = '123456'
# 运行时环境变量字典
environsDict = os.environ
#此次 jenkins 构建版本号
jenkins_build_number = environsDict['BUILD_NUMBER']

#项目名称，用在拼接 tomcat 文件地址
project_name = 'jenkins'



#获取apk文件路径
def get_apk_file_path():
    #工作目录下面的 apk 文件
    apk_file_workspace_path = '/Users/allanwendy/data/jenkins/jobs/jenkins/workspace/app/build/outputs/apk/'+file_name
    return apk_file_workspace_path


# while get_apk_file_path() is None:
#     time.sleep(5)

#apk 文件路径
apk_file_path = get_apk_file_path()
print apk_file_path

#请求字典编码
def _encode_multapkrt(params_dict):

    boundary = '----------%s' % hex(int(time.time() * 1000))
    data = []
    for k, v in params_dict.items():
        data.append('--%s' % boundary)

        if hasattr(v, 'read'):
            filename = getattr(v, 'name', '')
            content = v.read()
            decoded_content = content.decode('ISO-8859-1')
            data.append('Content-Disposition: form-data; name="%s"; filename="app-debug.apk"' % k)
            data.append('Content-Type: application/octet-stream\r\n')
            data.append(decoded_content)
        else:
            data.append('Content-Disposition: form-data; name="%s"\r\n' % k)
            data.append(v if isinstance(v, str) else v.decode('utf-8'))
    data.append('--%s--\r\n' % boundary)
    return '\r\n'.join(data), boundary


#处理 蒲公英 上传结果
def handle_resule(result):

    json_result = json.loads(result)


    print json_result


    if json_result['code'] is 0:
        send_Email(json_result)

#发送邮件
def send_Email(json_result):

    appName = json_result['data']['cn.wecook.app']
    appKey = json_result['data']['bd47bd1537c02d40a4b75e6b256341d9']
    appVersion = json_result['data']['2.5.6']
    appBuildVersion = json_result['data']['2.5.6']
    appShortcutUrl = json_result['data']['http://www.pgyer.com/SSJu']
    #邮件接受者
    mail_receiver = ['allan.wang@wecook.cn']
    #根据不同邮箱配置 host，user，和pwd
    mail_host = 'smtp.163.com'
    mail_user = 'wenyu757123@163.com'
    mail_pwd = '1984757'
    mail_to = 'allan.wang@wecook.cn'
    mail_title = project_name + '最新打包文件' + '(' +jenkins_build_number + ')'
    msg = MIMEMultapkrt()

    environsString = '<h3>本次打包相关信息</h3><p>'
    environsString += '<p>项目名称 : '+ project_name + '<p>'
    environsString += '<p>构建ID:' + jenkins_build_number +'<p>'
    environsString += '<p>你也可从蒲公英网站在线安装 : ' + 'http://www.pgyer.com/' + str(appShortcutUrl) + '   密码 : ' + installPassword + '<p>'
    environsString += '<li><a href="itms-services://?action=download-manifest&url=https://ssl.pgyer.com/app/plist/' + str(appKey) + '">点我直接安装</a></li>'
    message = environsString
    body = MIMEText(message, _subtype='html', _charset='utf-8')
    msg.attach(body)
    msg['To'] = mail_to
    msg['from'] = mail_user
    msg['subject'] =mail_title

    try:
        s = smtplib.SMTP()
        s.connect(mail_host)
        s.login(mail_user, mail_pwd)

        s.sendmail(mail_user, mail_receiver, msg.as_string())
        s.close()
        print 'success'
    except Exception, e:
        print e

#############################################################
#请求参数字典
params = {
    'uKey': uKey,
    '_api_key': _api_key,
    'file': open(apk_file_path, 'rb'),
    'publishRange': '2',
    'password': installPassword

}

coded_params, boundary = _encode_multapkrt(params)

f = {'file': open('app-debug.apk','rb')}
r = requests.post(url,files = f)
#req = urllib2.Request(url, coded_params.encode('ISO-8859-1'))
#req.add_header('Content-Type', 'multapkrt/form-data; boundary=%s' % boundary)
try:
    resp = urllib2.urlopen(req)
    body = resp.read().decode('utf-8')
    handle_resule(body)

except urllib2.HTTPError as e:
    print(e.fp.read())

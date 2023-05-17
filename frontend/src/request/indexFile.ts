import axios from 'axios'
import {getBaseURL} from "@/global/global";

//新创建用来传输文件的实例
const serviceFile = axios.create({
    baseURL: getBaseURL(),
    timeout: 60000,
    headers: {'Content-Type' : 'multipart/form-data'},
})
export let get =async (url,params) =>{
    let {data} =await serviceFile.get(url,{params})
    return data
}
export let post = async (url,params) =>{
    let {data} = await serviceFile.post(url,params)
    return data
}

export default serviceFile;

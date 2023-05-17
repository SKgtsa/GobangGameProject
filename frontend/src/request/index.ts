import axios from 'axios'
import {getBaseURL} from "@/global/global";

//已创建的axios实例
const service = axios.create({
    baseURL: getBaseURL(),
    timeout: 60000,
})
export let get =async (url,params) =>{
  let {data} =await service.get(url,{params})
  return data
}
export let post = async (url,params) =>{
  let {data} = await service.post(url,params)
  return data
}
export default service;


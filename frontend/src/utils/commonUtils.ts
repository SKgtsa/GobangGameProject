export const isNumber = (i: String): boolean => {
    try{
        Number(i);
    }catch (e){
        return false;
    }
    return true;
}

package com.projeto.sistema.controle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.projeto.sistema.modelos.Venda;
import com.projeto.sistema.modelos.ItemVenda;
import com.projeto.sistema.modelos.Produto;
import com.projeto.sistema.repositorios.VendaRepositorio;
import com.projeto.sistema.repositorios.ClienteRepositorio;
import com.projeto.sistema.repositorios.FuncionarioRepositorio;
import com.projeto.sistema.repositorios.ItemVendaRepositorio;
import com.projeto.sistema.repositorios.ProdutoRepositorio;

@Controller
public class VendaControle {
	
	@Autowired
	private VendaRepositorio vendaRepositorio;
	@Autowired
	private ItemVendaRepositorio itemVendaRepositorio;
	@Autowired
	private ProdutoRepositorio produtoRepositorio;
	@Autowired
	private FuncionarioRepositorio funcionarioRepositorio;
	@Autowired
	private ClienteRepositorio clienteRepositorio;
	
	private List<ItemVenda> listaItemVenda = new ArrayList<ItemVenda>();
	
	@GetMapping("/cadastroVenda")
	public ModelAndView cadastrar(Venda venda, ItemVenda itemVenda) {
		ModelAndView mv = new ModelAndView("administrativo/vendas/cadastro");
		mv.addObject("venda", venda);
		mv.addObject("itemVenda", itemVenda);
		mv.addObject("listaItemVenda", this.listaItemVenda);
		mv.addObject("listaFuncionarios", funcionarioRepositorio.findAll());
		mv.addObject("listaClientes", clienteRepositorio.findAll());
		mv.addObject("listaProdutos", produtoRepositorio.findAll());
		
		return mv;
	}
	
	@GetMapping("/listarVenda")
	public ModelAndView listar() {
		ModelAndView mv = new ModelAndView("administrativo/vendas/lista");
		mv.addObject("listaVendas", vendaRepositorio.findAll());
		return mv;
	}
	
	@GetMapping("/editarVenda/{id}")
	public ModelAndView editar(@PathVariable("id") Long id) {
		Optional<Venda> venda = vendaRepositorio.findById(id);
		this.listaItemVenda = itemVendaRepositorio.buscarPorVenda(venda.get().getId());
		
		return cadastrar(venda.get(), new ItemVenda());
		
	}
	
//	@GetMapping("/removerVenda/{id}")
//	public ModelAndView remover(@PathVariable("id") Long id) {
//		Optional<Venda> venda = vendaRepositorio.findById(id);
//		vendaRepositorio.delete(venda.get());
//		return listar(); 
//		
//	}
	
	@GetMapping("/removerItemVenda/{id}")
	public ModelAndView removerItem(@PathVariable("id") Long id, Venda venda) {
		Optional<ItemVenda> itemVenda = itemVendaRepositorio.findById(id);
		itemVendaRepositorio.delete(itemVenda.get());
		return cadastrar(venda, new ItemVenda());
		
	}
	
	
	
	@PostMapping("/salvarVenda")
	public ModelAndView salvar(String acao, Venda venda, ItemVenda itemVenda, BindingResult result) {
		if(result.hasErrors()) {
			return cadastrar(venda, itemVenda);
		}
		
		if(acao.equals("itens")) {
			itemVenda.setValor(itemVenda.getProduto().getPrecoVenda());
			itemVenda.setSubtotal(itemVenda.getProduto().getPrecoVenda() * itemVenda.getQuantidade());
			venda.setValorTotal(venda.getValorTotal() + (itemVenda.getValor() * itemVenda.getQuantidade()));
			venda.setQuantidadeTotal(venda.getQuantidadeTotal() + itemVenda.getQuantidade());
		
			this.listaItemVenda.add(itemVenda);
			
		}else if(acao.equals("salvar")) {
			vendaRepositorio.saveAndFlush(venda);
			
			for(ItemVenda it: listaItemVenda) {
				it.setVenda(venda);
//				it.setSubtotal(it.getValor()*it.getQuantidade());
				itemVendaRepositorio.saveAndFlush(it);
				
				Optional<Produto> prod = produtoRepositorio.findById(it.getProduto().getId());
				Produto produto = prod.get();
				produto.setEstoque(produto.getEstoque() - it.getQuantidade());
				produto.setPrecoVenda(it.getValor());
//				produto.setPrecoCusto(it.getValorCusto());
				produtoRepositorio.saveAndFlush(produto);
				this.listaItemVenda = new ArrayList<>();
			}
			return cadastrar(new Venda(), new ItemVenda());
		}
		return cadastrar(venda, new ItemVenda());
		
	
	}

	public List<ItemVenda> getListaItemVenda() {
		return listaItemVenda;
	}

	public void setListaItemVenda(List<ItemVenda> listaItemVenda) {
		this.listaItemVenda = listaItemVenda;
	}
	
	

}

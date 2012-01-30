package org.nutz.mongo.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;
import org.nutz.mongo.MongoCase;
import org.nutz.mongo.dao.pojo.CappedPet;
import org.nutz.mongo.dao.pojo.Pet;
import org.nutz.mongo.dao.pojo.PetType;
import org.nutz.mongo.dao.pojo.SInner;
import org.nutz.mongo.dao.pojo.SObj;
import org.nutz.mongo.util.Moo;
import org.nutz.mongo.util.MCur;

import com.mongodb.DB;
import com.mongodb.WriteResult;

public class MongoDaoPojoTest extends MongoCase {

	@Test
	public void test_save_inner_pojo() {
		dao.create(SObj.class, true);

		SObj o = SObj.create("abc");
		o.setObj(SInner.me(6, 9));
		dao.save(o);

		SObj o2 = dao.findById(SObj.class, o.getId());
		assertEquals(o.getName(), o2.getName());
		assertEquals(6, o2.getObj().getX());
		assertEquals(9, o2.getObj().getY());

		o.setObj(SInner.me(22, 33));
		dao.save(o);

		o2 = dao.findById(SObj.class, o.getId());
		assertEquals(o.getName(), o2.getName());
		assertEquals(22, o2.getObj().getX());
		assertEquals(33, o2.getObj().getY());

		dao.updateById(SObj.class, o.getId(), Moo.born().set("obj", SInner.me(44, 55)));

		o2 = dao.findById(SObj.class, o.getId());
		assertEquals(o.getName(), o2.getName());
		assertEquals(44, o2.getObj().getX());
		assertEquals(55, o2.getObj().getY());
	}

	@Test
	public void test_save_inner_objarray() {
		dao.create(SObj.class, true);

		SObj o = SObj.create("abc");
		o.setInners(Lang.array(SInner.me(45, 90), SInner.me(43, 62)));
		dao.save(o);

		SObj o2 = dao.findById(SObj.class, o.getId());
		assertEquals(o.getName(), o2.getName());
		assertEquals(2, o2.getInners().length);
		assertEquals(45, o2.getInners()[0].getX());
		assertEquals(90, o2.getInners()[0].getY());
		assertEquals(43, o2.getInners()[1].getX());
		assertEquals(62, o2.getInners()[1].getY());

		o.setInners(Lang.array(SInner.me(3, 5)));
		dao.save(o);

		o2 = dao.findById(SObj.class, o.getId());
		assertEquals(o.getName(), o2.getName());
		assertEquals(1, o2.getInners().length);
		assertEquals(3, o2.getInners()[0].getX());
		assertEquals(5, o2.getInners()[0].getY());

		dao.updateById(	SObj.class,
						o.getId(),
						Moo.born().set(	"inners",
										Lang.array(	SInner.me(77, 88),
													SInner.me(44, 33),
													SInner.me(55, 66))));

		o2 = dao.findById(SObj.class, o.getId());
		assertEquals(o.getName(), o2.getName());
		assertEquals(3, o2.getInners().length);
		assertEquals(77, o2.getInners()[0].getX());
		assertEquals(88, o2.getInners()[0].getY());
		assertEquals(44, o2.getInners()[1].getX());
		assertEquals(33, o2.getInners()[1].getY());
		assertEquals(55, o2.getInners()[2].getX());
		assertEquals(66, o2.getInners()[2].getY());

	}

	@Test
	public void test_save_inner_map() {
		dao.create(SObj.class, true);

		SObj o = SObj.create("abc");
		o.setMap("{x:1, y:36}");
		dao.save(o);

		SObj o2 = dao.findById(SObj.class, o.getId());
		assertEquals(o.getName(), o2.getName());
		assertEquals(1, ((Integer) o2.getMap().get("x")).intValue());
		assertEquals(36, ((Integer) o2.getMap().get("y")).intValue());

		o.setMap("{x:88, y:93}");
		dao.save(o);

		o2 = dao.findById(SObj.class, o.getId());
		assertEquals(o.getName(), o2.getName());
		assertEquals(88, ((Integer) o2.getMap().get("x")).intValue());
		assertEquals(93, ((Integer) o2.getMap().get("y")).intValue());

		dao.updateById(SObj.class, o.getId(), Moo.born().set("map", Lang.map("{x:100,y:300}")));

		o2 = dao.findById(SObj.class, o.getId());
		assertEquals(o.getName(), o2.getName());
		assertEquals(100, ((Integer) o2.getMap().get("x")).intValue());
		assertEquals(300, ((Integer) o2.getMap().get("y")).intValue());

	}

	@Test
	public void test_save_again_by_defaulID() {
		dao.create(SObj.class, true);

		// Create
		SObj obj = dao.save(SObj.create("xyz"));

		// Update
		obj.setNumber(3000);
		dao.save(obj);

		// Get back
		SObj obj2 = dao.findById(SObj.class, obj.getId());

		// Assert
		assertEquals(3000, obj2.getNumber());
	}

	@Test
	public void test_save_again_by_uu64() {
		dao.create(Pet.class, true);

		Pet xb = dao.save(Pet.me("xb"));
		xb.setCount(6000);

		dao.save(xb);
		Pet xb2 = dao.findById(Pet.class, xb.getId());

		assertEquals(6000, xb2.getCount());
	}

	@Test
	public void test_query_in_array() {
		dao.create(Pet.class, true);
		dao.save(Pet.me("xiaohei"));
		dao.save(Pet.me("xiaobai"));
		dao.save(Pet.me("super.man"));
		dao.save(Pet.me("bush"));
		dao.save(Pet.me("zozoh"));

		List<Pet> pets = dao.find(	Pet.class,
									Moo.born().in("name", "xiaohei", "zozoh"),
									MCur.born().asc("name"));
		assertEquals(2, pets.size());
		assertEquals("xiaohei", pets.get(0).getName());
		assertEquals("zozoh", pets.get(1).getName());
	}

	@Test
	public void test_query_by_labels() {
		dao.create(Pet.class, true);
		dao.save(Pet.mel("xiaohei", "A", "B", "C"));
		dao.save(Pet.mel("xiaobai", "A", "C"));
		dao.save(Pet.mel("super.man", "A", "B"));
		dao.save(Pet.mel("bush", "B"));
		dao.save(Pet.mel("zozoh", "C"));

		// 默认按照名字从小到大
		MCur c = MCur.born().asc("name");

		// A+C
		List<Pet> pets = dao.find(Pet.class, Moo.born().array("labels", "A", "C"), c);
		assertEquals(2, pets.size());
		assertEquals("xiaobai", pets.get(0).getName());
		assertEquals("xiaohei", pets.get(1).getName());

		// B+C
		pets = dao.find(Pet.class, Moo.born().array("labels", "B", "C"), c);
		assertEquals(1, pets.size());
		assertEquals("xiaohei", pets.get(0).getName());

		// C
		pets = dao.find(Pet.class, Moo.born().array("labels", "C"), c);
		assertEquals(3, pets.size());
		assertEquals("xiaobai", pets.get(0).getName());
		assertEquals("xiaohei", pets.get(1).getName());
		assertEquals("zozoh", pets.get(2).getName());

	}

	@Test
	public void test_query_by_regex() {
		dao.create(Pet.class, true);
		dao.save(Pet.me("xiaohei"));
		dao.save(Pet.me("xiaobai"));
		dao.save(Pet.me("super.man"));
		dao.save(Pet.me("bush"));
		dao.save(Pet.me("zozoh"));

		// startsWith
		List<Pet> pets = dao.find(	Pet.class,
									Moo.born().startsWith("name", "x"),
									MCur.born().desc("name"));
		assertEquals(2, pets.size());
		assertEquals("xiaohei", pets.get(0).getName());
		assertEquals("xiaobai", pets.get(1).getName());

		// contains
		pets = dao.find(Pet.class, Moo.born().contains("name", "h"), MCur.born().desc("name"));
		assertEquals(3, pets.size());
		assertEquals("zozoh", pets.get(0).getName());
		assertEquals("xiaohei", pets.get(1).getName());
		assertEquals("bush", pets.get(2).getName());

		// regex
		pets = dao.find(Pet.class,
						Moo.born().match("name", "^(zozoh)|(.*u[p|s].*)$"),
						MCur.born().desc("name"));
		assertEquals(3, pets.size());
		assertEquals("zozoh", pets.get(0).getName());
		assertEquals("super.man", pets.get(1).getName());
		assertEquals("bush", pets.get(2).getName());
	}

	@Test
	public void test_default_id_save_and_remove() {
		dao.create(SObj.class, true);
		SObj obj = SObj.create("ABC");
		dao.save(obj);
		assertEquals(1, dao.count(obj, null));

		obj = dao.findOne(SObj.class, Moo.born().append("name", "ABC"));

		dao.removeById(SObj.class, obj.getId());
		assertEquals(0, dao.count(obj, null));
	}

	@Test
	public void test_enum_type_field() {
		dao.create(Pet.class, true);
		Pet snoopy = dao.save(Pet.me(PetType.DOG, "Snoopy"));
		dao.save(snoopy);
		assertNotNull(snoopy.getId());

		Pet snoopy2 = dao.findById(Pet.class, snoopy.getId());
		assertEquals(snoopy.getType(), snoopy2.getType());

		dao.save(Pet.me(PetType.CAT, "Tom"));
		dao.update(	Pet.class,
					Moo.born().append("type", PetType.CAT),
					Moo.born().set("type", PetType.MONKEY).set("count", 3000));

		Pet tom = dao.findOne(Pet.class, Moo.born().append("name", "Tom"));
		assertEquals(3000, tom.getCount());
		assertEquals(PetType.MONKEY, tom.getType());
	}

	@Test
	public void test_simple_update() {
		dao.create(Pet.class, true);
		Pet xb = dao.save(Pet.me("XiaoBai"));

		WriteResult wr = dao.update(xb,
									Moo.born().append("id", xb.getId()),
									Moo.born().set("name", "XB").inc("age", 2));
		assertEquals(1, wr.getN());
		assertNull(wr.getError());

		Pet xb2 = dao.findById(Pet.class, xb.getId());
		assertEquals(xb.getAge() + 2, xb2.getAge());
		assertEquals("XB", xb2.getName());

		dao.updateById(Pet.class, xb.getId(), Moo.born().inc("count", 1));
		Pet xb3 = dao.findById(Pet.class, xb.getId());
		assertEquals(xb.getCount() + 1, xb3.getCount());
		assertEquals("XB", xb3.getName());

		wr = dao.update(xb, Moo.born().append("id", "888888"), Moo.born()
																	.set("name", "GGGGGG")
																	.inc("age", 2555));
		assertEquals(0, wr.getN());
		assertNull(wr.getError());
	}

	@Test
	public void test_simple_save_find() {
		dao.create(Pet.class, true);
		Pet xb = dao.save(Pet.me("XiaoBai"));
		Pet xb2 = dao.findById(Pet.class, xb.getId());
		assertEquals(xb.getName(), xb2.getName());
	}

	@Test
	public void test_simple_query() {
		dao.create(Pet.class, true);
		dao.save(Pet.me("XiaoBai", 10, 3));
		dao.save(Pet.me("XiaoHei", 20, 3));
		dao.save(Pet.me("SuperMan", 20, 3));
		dao.save(Pet.me("Bush", 10, 3));
		dao.save(Pet.me("XiaoQiang", 10, 3));

		List<Pet> pets = dao.find(Pet.class, Moo.born().append("age", 20), null);
		assertEquals(2, pets.size());

		pets = dao.find(Pet.class, null, MCur.born().asc("name"));
		assertEquals(5, pets.size());
		assertEquals("Bush", pets.get(0).getName());
		assertEquals("SuperMan", pets.get(1).getName());
		assertEquals("XiaoBai", pets.get(2).getName());
		assertEquals("XiaoHei", pets.get(3).getName());
		assertEquals("XiaoQiang", pets.get(4).getName());

	}

	// 测试唯一性索引
	@Test(expected = Throwable.class)
	public void test_index_unique() {
		dao.create(Pet.class, true);
		dao.save(Pet.me("XiaoBai", 10, 3));
		dao.runNoError(new Callback<DB>() {
			public void invoke(DB db) {
				dao.save(Pet.me("XiaoBai", 2, 222));
			}
		});
	}

	// 测试固定集合
	@Test
	public void test_capped() {
		dao.create(CappedPet.class, true); // 固定大小是10k,肯定放不到1000个对象
		for (int i = 0; i < 1000; i++) {
			CappedPet pet = new CappedPet();
			pet.setName("V" + System.currentTimeMillis());
			dao.save(pet);
		}
		long size = dao.count(CappedPet.class, null);
		System.out.println("CappedPet size=" + size);
		assertNotSame(1000, size);
	}
}
